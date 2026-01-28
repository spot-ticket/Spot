package com.example.Spot.payments.application.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.feign.OrderClient;
import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.UserClient;
import com.example.Spot.global.feign.dto.OrderResponse;
import com.example.Spot.global.presentation.advice.BillingKeyNotFoundException;
import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;
import com.example.Spot.payments.domain.entity.UserBillingAuthEntity;
import com.example.Spot.payments.domain.gateway.PaymentGateway;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.domain.repository.UserBillingAuthRepository;
import com.example.Spot.payments.infrastructure.aop.Cancel;
import com.example.Spot.payments.infrastructure.aop.PaymentBillingApproveTrace;
import com.example.Spot.payments.infrastructure.aop.Ready;
import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;
import com.example.Spot.payments.infrastructure.producer.PaymentEventProducer;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentGateway paymentGateway;
  private final PaymentEventProducer paymentEventProducer;
  private final PaymentHistoryService paymentHistoryService;

  @Value("${toss.payments.timeout}")
  private Integer timeout;

  private final PaymentRepository paymentRepository;
  private final PaymentHistoryRepository paymentHistoryRepository;
  private final PaymentRetryRepository paymentRetryRepository;
  private final PaymentKeyRepository paymentKeyRepository;
  private final UserBillingAuthRepository userBillingAuthRepository;

  // Feign Clients
  private final OrderClient orderClient;
  private final UserClient userClient;
  private final StoreClient storeClient;
  
  // 주문 수락 이후에 동작되어야 함
  // https://docs.tosspayments.com/reference/using-api/webhook-events 참고
  // ready -> createPaymentBillingApprove -> confirmBillingPayment
  @Ready
  public UUID ready(Integer userId, UUID orderId, PaymentRequestDto.Confirm request) {
    Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderId(orderId);
    
    if (existingPayment.isPresent()) {
      log.info("ℹ️ [이미 처리된 주문] 기존 결제 ID를 반환합니다. orderId={}", orderId);
      return existingPayment.get().getId(); //
    }
    PaymentEntity payment = createPayment(userId, orderId, request);
    return payment.getId();
  }


  // ******* //
  // 결제 승인 //
  // ******* //
  @PaymentBillingApproveTrace
  public PaymentResponseDto.Confirm createPaymentBillingApprove(UUID paymentId) {
    PaymentEntity payment = findPayment(paymentId);

    UserBillingAuthEntity billingAuth = userBillingAuthRepository
        .findActiveByUserId(payment.getUserId())
        .orElseThrow(() -> new BillingKeyNotFoundException(
            "[PaymentService] 등록된 결제 수단이 없습니다. 먼저 결제 수단을 등록해주세요. UserId: " + payment.getUserId()));

    UUID uniqueOrderId = payment.getOrderId();

    TossPaymentResponse response = confirmBillingPayment(payment, billingAuth, uniqueOrderId);
    
    paymentHistoryService.recordPaymentSuccess(paymentId, response.getPaymentKey());
    
    return PaymentResponseDto.Confirm.builder()
            .paymentId(paymentId)
            .amount(response.getTotalAmount())
            .paymentKey(response.getPaymentKey())
            .status("DONE") // 상태 정보 등
            .build();
  }

  private TossPaymentResponse confirmBillingPayment(PaymentEntity payment, UserBillingAuthEntity billingAuth, UUID uniqueOrderId) {

    String billingKey = billingAuth.getBillingKey();

    if (billingKey == null || billingKey.isEmpty()) {
        throw new IllegalStateException(
            "[PaymentService] 등록된 Billing Key가 없습니다. 먼저 Billing Key를 입력해주세요.");
    }

    return paymentGateway.requestBillingPayment(
        billingKey,
        payment.getTotalAmount(),
        uniqueOrderId,
        payment.getPaymentTitle(),
        billingAuth.getCustomerKey(),
        this.timeout);
  }

  // ******* //
  // 결제 취소_비동기 //
  // ******* //
  @Transactional
  public boolean refundByOrderId(UUID orderId) {
    // 1. 주문 ID로 결제 엔티티 조회
    PaymentEntity payment = paymentRepository.findActivePaymentByOrderId(orderId)
            .orElseThrow(() -> {
              log.warn("⚠️ [환불 스킵] 취소 가능한 결제 내역이 없거나 이미 취소되었습니다. OrderID: {}", orderId);
              return new ResourceNotFoundException("취소 가능한 결제 내역을 찾을 수 없습니다.");
            });
    
    // 2. 환불을 위한 DTO 조립
    PaymentRequestDto.Cancel cancelRequest = PaymentRequestDto.Cancel.builder()
            .paymentId(payment.getId())
            .cancelReason("주문 서비스 요청에 의한 보상 트랜잭션 수행")
            .build();
    
    // 3. 기존의 취소 로직 실행
    executeCancel(cancelRequest);
    
    return true;
  }
  
  // ******* //
  // 결제 취소 //
  // ******* //
  // 결제했을 때 발급받은 paymentKey를 이용함
  @Cancel
  public PaymentResponseDto.Cancel executeCancel(PaymentRequestDto.Cancel request) {
    
    PaymentKeyEntity paymentKeyEntity =
        paymentKeyRepository
            .findByPaymentId(request.paymentId())
            .orElseThrow(() -> new IllegalStateException("[PaymentService] 결제 키가 없어 취소할 수 없습니다."));
    
    paymentHistoryService.recordCancelProgress(request.paymentId());

    tossPaymentCancel(
        request.paymentId(), paymentKeyEntity.getPaymentKey(), request.cancelReason());

    paymentHistoryService.recordCancelSuccess(request.paymentId());

    return PaymentResponseDto.Cancel.builder()
        .paymentId(request.paymentId())
        .cancelAmount(0L)
        .cancelReason(request.cancelReason())
        .canceledAt(LocalDateTime.now())
        .build();
  }

  private TossPaymentResponse tossPaymentCancel( UUID paymentId, String paymentKey, String cancelReason) {
      return paymentGateway.cancelPayment(paymentKey, cancelReason, this.timeout);
  }

  // ******* //
  // 부분 취소 //
  // ******* //
  public PaymentResponseDto.PartialCancel executePartialCancel(
      PaymentRequestDto.PartialCancel request) {
    return null;
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

  // Order MSA -> Here
  private OrderResponse findOrder(UUID orderId) {
    OrderResponse order = orderClient.getOrderById(orderId);
    if (order == null) {
      throw new ResourceNotFoundException("[PaymentService] 주문을 찾을 수 없습니다.");
    }
    return order;
  }

  // Order 서비스 호출 - 주문 존재 확인
  private void validateOrderExists(UUID orderId) {
    if (!orderClient.existsById(orderId)) {
      throw new ResourceNotFoundException("[PaymentService] 주문을 찾을 수 없습니다.");
    }
  }



  // User 서비스 호출 - 사용자 존재 확인
  @CircuitBreaker(name = "user_validate_activeUser")
  @Bulkhead(name = "user_validate_activeUser", type = Bulkhead.Type.SEMAPHORE)
  @Retry(name = "user_validate_activeUser")
  private void validateUserExists(Integer userId) {
    if (!userClient.existsById(userId)) {
      throw new ResourceNotFoundException("[PaymentService] 사용자를 찾을 수 없습니다.");
    }
  }

  private PaymentEntity findPayment(UUID paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 결제를 찾을 수 없습니다."));
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
            PaymentHistoryEntity.PaymentStatus.PARTIAL_CANCELLED);

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

  // ************************ //
  // OWNER 가게 소유권 검증 메서드 //
  // ************************ //

  public void validateOrderStoreOwnership(UUID orderId, Integer userId) {
    // Order 서비스에서 주문 조회
    OrderResponse order = findOrder(orderId);
    UUID storeId = order.getStoreId();
    // Store 서비스에서 소유권 검증
    if (!storeClient.existsByStoreIdAndUserId(storeId, userId)) {
      throw new IllegalStateException("[PaymentService] 해당 주문의 가게에 대한 접근 권한이 없습니다.");
    }
  }

  public void validatePaymentStoreOwnership(UUID paymentId, Integer userId) {
    PaymentEntity payment = findPayment(paymentId);
    // Order 서비스에서 주문 조회
    OrderResponse order = findOrder(payment.getOrderId());
    UUID storeId = order.getStoreId();
    // Store 서비스에서 소유권 검증
    if (!storeClient.existsByStoreIdAndUserId(storeId, userId)) {
      throw new IllegalStateException("[PaymentService] 해당 결제의 가게에 대한 접근 권한이 없습니다.");
    }
  }

  // *************** //
  // 빌링 인증 정보 저장 //
  // *************** //
  @Transactional
  public PaymentResponseDto.SavedBillingKey saveBillingKey(PaymentRequestDto.SaveBillingKey request) {
    // User 서비스에서 사용자 존재 확인
    validateUserExists(request.userId());

    // 기존 빌링 인증 정보가 있다면 비활성화 (사용자당 하나의 활성 인증 정보만 유지)
    userBillingAuthRepository.findActiveByUserId(request.userId())
        .ifPresent(existing -> {
          existing.deactivate();
          userBillingAuthRepository.save(existing);
        });

          // 빌링키 발행
    TossPaymentResponse billingKeyResponse = paymentGateway.issueBillingKey(
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

  // ************************** //
  // FE 결제 완료 후 결제 내역 저장 //
  // ************************** //
  @Transactional
  public PaymentResponseDto.SavedPaymentHistory savePaymentHistory(PaymentRequestDto.SavePaymentHistory request) {
    // 사용자 존재 확인
    validateUserExists(request.userId());
    // 주문 존재 확인
    validateOrderExists(request.orderId());

    // Payment 엔티티 생성 및 저장
    PaymentEntity payment = PaymentEntity.builder()
        .userId(request.userId())
        .orderId(request.orderId())
        .title(request.title())
        .content(request.content())
        .paymentMethod(request.paymentMethod())
        .totalAmount(request.paymentAmount())
        .build();

    PaymentEntity savedPayment = paymentRepository.save(payment);

    // PaymentHistory에 DONE 상태로 저장
    PaymentHistoryEntity history = PaymentHistoryEntity.builder()
        .paymentId(savedPayment.getId())
        .status(PaymentHistoryEntity.PaymentStatus.DONE)
        .build();

    paymentHistoryRepository.save(history);

    // PaymentKey 저장
    PaymentKeyEntity paymentKey = PaymentKeyEntity.builder()
        .paymentId(savedPayment.getId())
        .paymentKey(request.paymentKey())
        .confirmedAt(LocalDateTime.now())
        .build();

    paymentKeyRepository.save(paymentKey);
    
    // 결제 성공 이벤트 발행(수동)
    paymentEventProducer.reservePaymentSucceededEvent(
            savedPayment.getOrderId(),
            savedPayment.getUserId()
    );

    return PaymentResponseDto.SavedPaymentHistory.builder()
        .paymentId(savedPayment.getId())
        .orderId(savedPayment.getOrderId())
        .status("DONE")
        .amount(savedPayment.getTotalAmount())
        .savedAt(LocalDateTime.now())
        .build();
  }
}
