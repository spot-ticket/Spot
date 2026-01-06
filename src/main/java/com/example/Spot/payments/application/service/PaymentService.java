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
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;

    @Value("${toss.payments.billingKey}")
    private String billingKey;

    @Value("${toss.payments.customerKey}")
    private String customerKey;

    @Value("${toss.payments.timeout}")
    private Integer timeout;

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentRetryRepository paymentRetryRepository;
    private final PaymentKeyRepository paymentKeyRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // 주문 수락 이후에 동작되어야 함
    // 프론트가 없으니까 자동 결제로 대체. 결제는 프론트 페이지가 요구됨
    // billingKey를 받으려면 프론트 로직이 요구되어서 customer_id는 1로 고정됨
    // https://docs.tosspayments.com/reference/using-api/webhook-events 참고

    // READY -> IN_PROGRESS -> DONE / ABORTED
    // 하나의 로직을 3단계로 나눔. 여러 장애로 결제 실패 이유를 알기 위함.
    // preparePayment -> executePaymentBilling -> executeTossPayment -> success or fail

    @Transactional
    public UUID preparePayment(PaymentRequestDto.Confirm request) {
        validatePaymentRequest(request);

        UserEntity user = findUser(request.userId());
        OrderEntity order = findOrder(request.orderId());

        PaymentEntity payment = createPayment(user.getId(), order.getId(), request);

        createPaymentHistory(payment.getId(), PaymentHistoryEntity.PaymentStatus.READY);

        return payment.getId();
    }

    @Transactional
    public PaymentResponseDto.Confirm executePaymentBilling(UUID paymentId) {
        recordProgress(paymentId);

        TossPaymentResponse response = executeTossPayment(paymentId);

        PaymentHistoryEntity paymentHistory = recordSuccess(paymentId, response.getPaymentKey());

        return PaymentResponseDto.Confirm.builder()
            .paymentId(paymentId)
            .status(paymentHistory.getStatus().toString())
            .amount(response.getAmount())
            .build();
    }

    @Transactional
    private void recordProgress(UUID paymentId) {
        PaymentEntity payment = findPayment(paymentId);

        PaymentHistoryEntity latestItem = paymentHistoryRepository
            .findTopByPaymentIdOrderByCreatedAtDesc(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("결제 이력을 찾을 수 없습니다."));

        if (latestItem.getStatus() != PaymentHistoryEntity.PaymentStatus.READY) {
            throw new IllegalStateException("이미 처리된 결제입니다");
        }

        createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.IN_PROGRESS);
    }

    @Transactional
    private void recordFailure(UUID paymentId, Exception e) {
        PaymentHistoryEntity paymentHistory = createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.ABORTED);
        createPaymentRetry(paymentId, paymentHistory.getId(), e.getMessage());
    }

    @Transactional
    private PaymentHistoryEntity recordSuccess(UUID paymentId, String paymentKey) {
        PaymentHistoryEntity paymentHistory = createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.DONE);
        createPaymentKey(paymentId, paymentKey, LocalDateTime.now());

        return paymentHistory;
    }

    private TossPaymentResponse executeTossPayment(UUID paymentId) {
        PaymentEntity payment = findPayment(paymentId);

        try {
            return tossPaymentClient.requestBillingPayment(
                    this.billingKey,
                    payment.getTotalAmount(),
                    paymentId.toString(),
                    payment.getPaymentTitle(),
                    this.customerKey,
                    this.timeout
            );
        } catch (Exception e) {
            recordFailure(paymentId, e);
            throw new RuntimeException("자동결제 실패: " + e.getMessage(), e);
        }
    }

    // // 결제했을 때 발급받은 paymentKey를 이용함
    // @Transactional
    // public PaymentResponseDto.Cancel cancelPayment(PaymentRequestDto.Cancel request) {

    //     PaymentEntity payment = paymentRepository.findById(request.paymentId())
    //             .orElseThrow(() -> new ResourceNotFoundException("결제를 찾을 수 없습니다."));

    //     // PaymentHistory에서 최신 해당 payment에 대해서 최신 것의 status를 확인
    //     PaymentHistoryEntity latestItem = paymentHistoryRepository
    //             .findTopByPaymentOrderByCreatedAtDesc(payment.getId())
    //             .orElseThrow(() -> new ResourceNotFoundException("결제 이력을 찾을 수 없습니다."));

    //     if (latestItem.getStatus() != PaymentHistoryEntity.PaymentStatus.DONE) {
    //         throw new IllegalStateException("완료된 결제만 취소할 수 있습니다. 현재 상태: " + latestItem.getStatus());
    //     }

    //     if (payment.getPaymentKey() == null) {
    //         throw new IllegalStateException("결제 키가 없어 취소할 수 없습니다.");
    //     }

    //     try {
    //         tossPaymentClient.cancelPayment(
    //                 payment.getPaymentKey(),
    //                 request.cancelReason(),
    //                 this.timeout
    //         );

    //         PaymentHistoryEntity cancelledItem = createPaymentHistory(payment.getId(), PaymentHistoryEntity.PaymentStatus.CANCELLED);

    //         return PaymentResponseDto.Cancel.builder()
    //                                         .paymentId(payment.getId())
    //                                         .cancelAmount(payment.getTotalAmount())
    //                                         .cancelReason(request.cancelReason())
    //                                         .canceledAt(cancelEntity.getCreatedAt())
    //                                         .build();

    //     } catch (Exception e) {
    //         throw new RuntimeException("결제 취소 실패: " + e.getMessage(), e);
    //     }
    // }

    private void validatePaymentRequest(PaymentRequestDto.Confirm request) {
        if (request.paymentAmount() <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다");
        }
    }

    private OrderEntity findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
    }

    private UserEntity findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private PaymentEntity findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("결제를 찾을 수 없습니다."));
    }

    private PaymentEntity createPayment(Integer userId, UUID orderId, PaymentRequestDto.Confirm request) {
        PaymentEntity payment = PaymentEntity.builder()
                                             .userId(userId)
                                             .orderId(orderId)
                                             .title(request.title())
                                             .content(request.content())
                                             .paymentMethod(request.paymentMethod())
                                             .totalAmount(request.paymentAmount())
                                             .build();
        
        return paymentRepository.save(payment);
    }

    private PaymentHistoryEntity createPaymentHistory(UUID paymentId, PaymentHistoryEntity.PaymentStatus status) {
        PaymentHistoryEntity paymentHistory = PaymentHistoryEntity.builder()
                                                                  .paymentId(paymentId)
                                                                  .status(status)
                                                                  .build();

        return paymentHistoryRepository.save(paymentHistory);
    }

    private PaymentKeyEntity createPaymentKey(UUID paymentId, String paymentKey, LocalDateTime confirmedAt) {
        PaymentKeyEntity paymentKeyEntity = PaymentKeyEntity.builder()
                                                      .paymentId(paymentId)
                                                      .paymentKey(paymentKey)
                                                      .confirmedAt(confirmedAt)
                                                      .build();
        return paymentKeyRepository.save(paymentKeyEntity);
    }

    private PaymentRetryEntity createPaymentRetry(UUID paymentId, UUID paymentHistoryId, String exception) {
        PaymentRetryEntity paymentRetry = PaymentRetryEntity.builder()
                                                            .paymentId(paymentId)
                                                            .failedPaymentHistoryId(paymentHistoryId)
                                                            .maxRetryCount(10)
                                                            .strategy(PaymentRetryEntity.RetryStrategy.FIXED_INTERVAL)
                                                            .nextRetryAt(LocalDateTime.now().plusMinutes(5))
                                                            .build();

        return paymentRetryRepository.save(paymentRetry);
    }

    public PaymentResponseDto.PaymentList getAllPayment() {

        payment
        return null;
    }

    public PaymentResponseDto.PaymentDetail getDetailPayment(UUID paymentId) {
        return null;
    }

    public PaymentResponseDto.CancelList getAllPaymentCancel() {
        return null;
    }

    public PaymentResponseDto.CancelList getDetailPaymentCancel(UUID paymentId) {
        return null;
    }
}
