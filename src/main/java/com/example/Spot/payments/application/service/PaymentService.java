package com.example.Spot.payments.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;
import com.example.Spot.payments.domain.entity.PaymentRetryEntity;
import com.example.Spot.payments.domain.entity.UserBillingAuthEntity;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.domain.repository.UserBillingAuthRepository;
import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.store.domain.repository.StoreUserRepository;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final TossPaymentClient tossPaymentClient;

  @Value("${toss.payments.timeout}")
  private Integer timeout;

  private final PaymentRepository paymentRepository;
  private final PaymentHistoryRepository paymentHistoryRepository;
  private final PaymentRetryRepository paymentRetryRepository;
  private final PaymentKeyRepository paymentKeyRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final StoreUserRepository storeUserRepository;
  private final UserBillingAuthRepository userBillingAuthRepository;

  // 주문 수락 이후에 동작되어야 함
  // https://docs.tosspayments.com/reference/using-api/webhook-events 참고

  // preparePayment -> executePaymentBilling -> executeTossPayment -> success or fail

  @Transactional
  public UUID preparePayment(PaymentRequestDto.Confirm request) {
    validatePaymentRequest(request);

    // 멱등성 체크: 동일 주문에 대해 진행 중이거나 완료된 결제가 있는지 확인
    paymentRepository
        .findActivePaymentByOrderId(request.orderId())
        .ifPresent(
            existingPayment -> {
              throw new IllegalStateException(
                  "[PaymentService] 이미 해당 주문에 대한 결제가 진행 중이거나 완료되었습니다. paymentId: " + existingPayment.getId());
            });

    UserEntity user = findUser(request.userId());
    OrderEntity order = findOrder(request.orderId());

    PaymentEntity payment = createPayment(user.getId(), order.getId(), request);

    createPaymentHistory(payment.getId(), PaymentHistoryEntity.PaymentStatus.READY);

    return payment.getId();
  }

  // ******* //
  // 결제 승인 //
  // ******* //
  public PaymentResponseDto.Confirm executePaymentBilling(UUID paymentId) {
    recordPaymentProgress(paymentId);

    TossPaymentResponse response = executeTossPaymentBilling(paymentId);

    PaymentHistoryEntity paymentHistory = recordPaymentSuccess(paymentId, response.getPaymentKey());

    return PaymentResponseDto.Confirm.builder()
        .paymentId(paymentId)
        .status(paymentHistory.getStatus().toString())
        .amount(response.getTotalAmount())
        .build();
  }

  @Transactional
  private void recordPaymentProgress(UUID paymentId) {
    PaymentHistoryEntity latestItem =
        paymentHistoryRepository
            .findTopByPaymentIdOrderByCreatedAtDesc(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 결제 이력을 찾을 수 없습니다."));

    if (latestItem.getStatus() != PaymentHistoryEntity.PaymentStatus.READY) {
      throw new IllegalStateException("[PaymentService] 이미 처리된 결제입니다");
    }

    createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.IN_PROGRESS);
  }

  @Transactional
  private void recordFailure(UUID paymentId, Exception e) {
    PaymentHistoryEntity paymentHistory =
        createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.ABORTED);
    createPaymentRetry(paymentId, paymentHistory.getId(), e.getMessage());
  }

  @Transactional
  private PaymentHistoryEntity recordPaymentSuccess(UUID paymentId, String paymentKey) {
    PaymentHistoryEntity paymentHistory =
        createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.DONE);
    createPaymentKey(paymentId, paymentKey, LocalDateTime.now());

    return paymentHistory;
  }

  // [Toss]
  private TossPaymentResponse executeTossPaymentBilling(UUID paymentId) {
    PaymentEntity payment = findPayment(paymentId);

    UserBillingAuthEntity billingAuth = userBillingAuthRepository
        .findActiveByUserId(payment.getUserId())
        .orElseThrow(() -> new IllegalStateException(
            "[PaymentService] 등록된 결제 수단이 없습니다. 먼저 결제 수단을 등록해주세요. UserId: " + payment.getUserId()));

    try {
      String billingKey = billingAuth.getBillingKey();

      if (billingKey == null || billingKey.isEmpty()) {
        throw new IllegalStateException(
            "[PaymentService] 등록된 Billing Key가 없습니다. 먼저 Billing Key를 입력해주세요.");
      }

      UUID uniqueOrderId = payment.getOrderId();

      return tossPaymentClient.requestBillingPayment(
          billingKey,
          payment.getTotalAmount(),
          uniqueOrderId,
          payment.getPaymentTitle(),
          billingAuth.getCustomerKey(),
          this.timeout);
    } catch (Exception e) {
      
      recordFailure(paymentId, e);
      throw new RuntimeException("[PaymentService] 자동결제 실패: " + e.getMessage(), e);
    }
  }

  // ******* //
  // 결제 취소 //
  // ******* //

  // 결제했을 때 발급받은 paymentKey를 이용함
  public PaymentResponseDto.Cancel executeCancel(PaymentRequestDto.Cancel request) {
    PaymentEntity payment = findPayment(request.paymentId());

    recordCancelProgress(request.paymentId());

    PaymentKeyEntity paymentKeyEntity =
        paymentKeyRepository
            .findByPaymentId(request.paymentId())
            .orElseThrow(() -> new IllegalStateException("[PaymentService] 결제 키가 없어 취소할 수 없습니다."));

        tossPaymentCancel(
            request.paymentId(), paymentKeyEntity.getPaymentKey(), request.cancelReason());
            
    PaymentHistoryEntity cancelledHistory = recordCancelSuccess(request.paymentId());

    return PaymentResponseDto.Cancel.builder()
        .paymentId(payment.getId())
        .cancelAmount(payment.getTotalAmount())
        .cancelReason(request.cancelReason())
        .canceledAt(cancelledHistory.getCreatedAt())
        .build();
  }

  private TossPaymentResponse tossPaymentCancel(
      UUID paymentId, String paymentKey, String cancelReason) {
    try {
      return tossPaymentClient.cancelPayment(paymentKey, cancelReason, this.timeout);
    } catch (Exception e) {
      recordFailure(paymentId, e);
      throw new RuntimeException("[PaymentService] 결제 취소 실패: " + e.getMessage(), e);
    }
  }

  @Transactional
  private void recordCancelProgress(UUID paymentId) {
    PaymentHistoryEntity latestItem =
        paymentHistoryRepository
            .findTopByPaymentIdOrderByCreatedAtDesc(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 결제 이력을 찾을 수 없습니다."));

    if (latestItem.getStatus() != PaymentHistoryEntity.PaymentStatus.DONE) {
      throw new IllegalStateException("[PaymentService] 결제 완료된 내역만 취소 가능합니다.");
    }

    createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.CANCELLED_IN_PROGRESS);
  }

  @Transactional
  private PaymentHistoryEntity recordCancelSuccess(UUID paymentId) {
    return createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.CANCELLED);
  }

  // ******* //
  // 부분 취소 //
  // ******* //
  public PaymentResponseDto.PartialCancel executePartialCancel(
      PaymentRequestDto.PartialCancel request) {
    return null;
  }

  private void validatePaymentRequest(PaymentRequestDto.Confirm request) {
    if (request.paymentAmount() <= 0) {
      throw new IllegalArgumentException("[PaymentService] 결제 금액은 0보다 커야 합니다");
    }
  }

  private OrderEntity findOrder(UUID orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 주문을 찾을 수 없습니다."));
  }

  private UserEntity findUser(Integer userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 사용자를 찾을 수 없습니다."));
  }

  private PaymentEntity findPayment(UUID paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 결제를 찾을 수 없습니다."));
  }

  private PaymentEntity createPayment(
      Integer userId, UUID orderId, PaymentRequestDto.Confirm request) {
    PaymentEntity payment =
        PaymentEntity.builder()
            .userId(userId)
            .orderId(orderId)
            .title(request.title())
            .content(request.content())
            .paymentMethod(request.paymentMethod())
            .totalAmount(request.paymentAmount())
            .build();

    return paymentRepository.save(payment);
  }

  private PaymentHistoryEntity createPaymentHistory(
      UUID paymentId, PaymentHistoryEntity.PaymentStatus status) {
    PaymentHistoryEntity paymentHistory =
        PaymentHistoryEntity.builder().paymentId(paymentId).status(status).build();

    return paymentHistoryRepository.save(paymentHistory);
  }

  private PaymentKeyEntity createPaymentKey(
      UUID paymentId, String paymentKey, LocalDateTime confirmedAt) {
    PaymentKeyEntity paymentKeyEntity =
        PaymentKeyEntity.builder()
            .paymentId(paymentId)
            .paymentKey(paymentKey)
            .confirmedAt(confirmedAt)
            .build();
    return paymentKeyRepository.save(paymentKeyEntity);
  }

  private PaymentRetryEntity createPaymentRetry(
      UUID paymentId, UUID paymentHistoryId, String exception) {
    PaymentRetryEntity paymentRetry =
        PaymentRetryEntity.builder()
            .paymentId(paymentId)
            .failedPaymentHistoryId(paymentHistoryId)
            .maxRetryCount(10)
            .strategy(PaymentRetryEntity.RetryStrategy.FIXED_INTERVAL)
            .nextRetryAt(LocalDateTime.now().plusMinutes(5))
            .build();

    return paymentRetryRepository.save(paymentRetry);
  }

  // *************** //
  // 결제 내역 전체 조회 //
  // *************** //
  @Transactional(readOnly = true)
  public PaymentResponseDto.PaymentList getAllPayment() {
    List<Object[]> results = paymentRepository.findAllPaymentsWithLatestStatus();

    List<PaymentResponseDto.PaymentDetail> payments =
        results.stream().map(this::mapToPaymentDetail).collect(Collectors.toList());

    return PaymentResponseDto.PaymentList.builder()
        .payments(payments)
        .totalCount(payments.size())
        .build();
  }

  @Transactional(readOnly = true)
  public PaymentResponseDto.PaymentDetail getDetailPayment(UUID paymentId) {
    List<Object[]> results = paymentRepository.findPaymentWithLatestStatus(paymentId);

    if (results.isEmpty()) {
      throw new ResourceNotFoundException("[PaymentService] 결제를 찾을 수 없습니다.");
    }

    return mapToPaymentDetail(results.get(0));
  }

  @Transactional(readOnly = true)
  public PaymentResponseDto.CancelList getAllPaymentCancel() {
    List<Object[]> results =
        paymentHistoryRepository.findAllByStatusWithPayment(
            PaymentHistoryEntity.PaymentStatus.CANCELLED);

    List<PaymentResponseDto.CancelDetail> cancellations =
        results.stream().map(row -> mapToCancelDetail(row, null)).collect(Collectors.toList());

    return PaymentResponseDto.CancelList.builder()
        .cancellations(cancellations)
        .totalCount(cancellations.size())
        .build();
  }

  @Transactional(readOnly = true)
  public PaymentResponseDto.CancelList getDetailPaymentCancel(UUID paymentId) {
    List<PaymentHistoryEntity.PaymentStatus> cancelStatuses =
        List.of(
            PaymentHistoryEntity.PaymentStatus.CANCELLED,
            PaymentHistoryEntity.PaymentStatus.PARTIAL_CANCELD);

    List<Object[]> results =
        paymentHistoryRepository.findByPaymentIdAndStatusesWithPayment(paymentId, cancelStatuses);

    List<PaymentResponseDto.CancelDetail> cancellations =
        results.stream().map(row -> mapToCancelDetail(row, null)).collect(Collectors.toList());

    return PaymentResponseDto.CancelList.builder()
        .cancellations(cancellations)
        .totalCount(cancellations.size())
        .build();
  }

  private PaymentResponseDto.PaymentDetail mapToPaymentDetail(Object[] row) {
    return PaymentResponseDto.PaymentDetail.builder()
        .paymentId((UUID) row[0])
        .title((String) row[1])
        .content((String) row[2])
        .paymentMethod((PaymentEntity.PaymentMethod) row[3])
        .totalAmount((Long) row[4])
        .status(((PaymentHistoryEntity.PaymentStatus) row[5]).toString())
        .createdAt((LocalDateTime) row[6])
        .build();
  }

  private PaymentResponseDto.CancelDetail mapToCancelDetail(Object[] row, String cancelReason) {
    return PaymentResponseDto.CancelDetail.builder()
        .cancelId((UUID) row[0])
        .paymentId((UUID) row[1])
        .cancelAmount((Long) row[2])
        .cancelReason(cancelReason)
        .canceledAt((LocalDateTime) row[3])
        .build();
  }

  // ***************** //
  // 소유권 검증 메서드 //
  // ***************** //

  public void validateOrderOwnership(UUID orderId, Integer userId) {
    OrderEntity order = findOrder(orderId);
    if (!order.getUserId().equals(userId)) {
      throw new IllegalStateException("[PaymentService] 해당 주문에 대한 접근 권한이 없습니다.");
    }
  }

  public void validatePaymentOwnership(UUID paymentId, Integer userId) {
    PaymentEntity payment = findPayment(paymentId);
    if (!payment.getUserId().equals(userId)) {
      throw new IllegalStateException("[PaymentService] 해당 결제에 대한 접근 권한이 없습니다.");
    }
  }

  public Integer getPaymentOwnerId(UUID paymentId) {
    PaymentEntity payment = findPayment(paymentId);
    return payment.getUserId();
  }

  // ************************ //
  // OWNER 가게 소유권 검증 메서드 //
  // ************************ //

  public void validateOrderStoreOwnership(UUID orderId, Integer userId) {
    OrderEntity order = findOrder(orderId);
    UUID storeId = order.getStore().getId();
    if (!storeUserRepository.existsByStoreIdAndUserId(storeId, userId)) {
      throw new IllegalStateException("[PaymentService] 해당 주문의 가게에 대한 접근 권한이 없습니다.");
    }
  }

  public void validatePaymentStoreOwnership(UUID paymentId, Integer userId) {
    PaymentEntity payment = findPayment(paymentId);
    OrderEntity order = findOrder(payment.getOrderId());
    UUID storeId = order.getStore().getId();
    if (!storeUserRepository.existsByStoreIdAndUserId(storeId, userId)) {
      throw new IllegalStateException("[PaymentService] 해당 결제의 가게에 대한 접근 권한이 없습니다.");
    }
  }

  // *************** //
  // 빌링 인증 정보 저장 //
  // *************** //
  @Transactional
  public PaymentResponseDto.SavedBillingKey saveBillingKey(PaymentRequestDto.SaveBillingKey request) {
    // 사용자 존재 확인
    findUser(request.userId());

    // 기존 빌링 인증 정보가 있다면 비활성화 (사용자당 하나의 활성 인증 정보만 유지)
    userBillingAuthRepository.findActiveByUserId(request.userId())
        .ifPresent(existing -> {
          existing.deactivate();
          userBillingAuthRepository.save(existing);
        });

          // 빌링키 발행
    TossPaymentResponse billingKeyResponse = tossPaymentClient.issueBillingKey(
          request.authKey(),
          request.customerKey()
    );

    // authKey와 customerKey 저장
    UserBillingAuthEntity billingAuth = UserBillingAuthEntity.builder()
        .userId(request.userId())
        .authKey(request.authKey())
        .customerKey(request.customerKey())
        .billingKey(billingKeyResponse.getBillingKey())
        .issuedAt(LocalDateTime.now())
        .build();

    userBillingAuthRepository.save(billingAuth);

    return PaymentResponseDto.SavedBillingKey.builder()
        .userId(request.userId())
        .customerKey(request.customerKey())
        .billingKey(billingAuth.getBillingKey())
        .savedAt(billingAuth.getIssuedAt())
        .build();
  }

  // *************** //
  // 빌링 인증 정보 존재 여부 확인 //
  // *************** //
  @Transactional(readOnly = true)
  public boolean hasBillingAuth(Integer userId) {

    boolean exists = userBillingAuthRepository.existsByUserIdAndIsActiveTrue(userId);

    // 상세 조회로 실제 데이터 확인
    userBillingAuthRepository.findActiveByUserId(userId)
        .ifPresentOrElse(
            auth -> System.out.println("실제 데이터 존재 - authKey: " + auth.getAuthKey() + ", isActive: " + auth.getIsActive()),
            () -> System.out.println("실제 데이터 없음")
        );

    return exists;
  }
}
