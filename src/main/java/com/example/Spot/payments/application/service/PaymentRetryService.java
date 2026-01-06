// package com.example.Spot.payments.application.service;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.UUID;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
// import com.example.Spot.order.domain.entity.OrderEntity;
// import com.example.Spot.payments.domain.entity.PaymentEntity;
// import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
// import com.example.Spot.payments.domain.entity.PaymentRetryEntity;
// import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
// import com.example.Spot.payments.domain.repository.PaymentRepository;
// import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
// import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
// import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class PaymentRetryService {

//     private final PaymentRetryRepository paymentRetryRepository;
//     private final PaymentRepository paymentRepository;
//     private final PaymentHistoryRepository paymentHistoryRepository;
//     private final TossPaymentClient tossPaymentClient;

//     @Transactional
//     public PaymentRetryEntity createRetryEntry(
//         PaymentEntity payment,
//         PaymentHistoryRepository failedHistory,
//         String errorMessage,
//         String errorCode
//     ) {
//         // 이미 재시도 항목이 있는지 확인
//         paymentRetryRepository.findByPaymentAndStatus(payment, PaymentRetryEntity.RetryStatus.PENDING)
//             .ifPresent(existing -> {
//                 throw new IllegalStateException("이미 재시도 대기 중인 결제입니다");
//             });

//         // 재시도 전략 결정 (에러 코드에 따라)
//         PaymentRetryEntity.RetryStrategy strategy = determineStrategy(errorCode);
//         int maxRetries = determineMaxRetries(errorCode);

//         PaymentRetryEntity retry = PaymentRetryEntity.builder()
//             .payment(payment)
//             .failedPaymentHistoryId(failedHistory)
//             .maxRetryCount(maxRetries)
//             .strategy(strategy)
//             .nextRetryAt(null) // 자동 계산됨
//             .build();

//         retry.recordFailedAttempt(errorMessage, errorCode);

//         return paymentRetryRepository.save(retry);
//     }

//     /**
//      * 재시도 실행
//      */
//     @Transactional
//     public void executeRetry(UUID retryId) {
//         PaymentRetryEntity retry = paymentRetryRepository.findById(retryId)
//             .orElseThrow(() -> new ResourceNotFoundException("재시도 항목을 찾을 수 없습니다"));

//         if (!retry.canRetry()) {
//             log.warn("재시도 불가능한 상태입니다. RetryId: {}, Status: {}", retryId, retry.getStatus());
//             return;
//         }

//         PaymentEntity payment = retry.getPayment();
//         OrderEntity order = retry.getFailedPaymentItem().getOrder();

//         try {
//             log.info("결제 재시도 시작. PaymentId: {}, Attempt: {}/{}",
//                 payment.getId(), retry.getAttemptCount() + 1, retry.getMaxRetryCount());

//             // IN_PROGRESS 상태 기록
//             PaymentItemEntity progressItem = createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.IN_PROGRESS);

//             // Toss API 재호출
//             TossPaymentResponse response = tossPaymentClient.requestBillingPayment(
//                 getBillingKey(),
//                 payment.getTotalAmount(),
//                 order.getId().toString(),
//                 payment.getPaymentTitle(),
//                 getCustomerKey()
//             );

//             // 성공 처리
//             payment.confirm(response.getPaymentKey());
//             createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.DONE);
//             retry.markAsSucceeded();

//             log.info("결제 재시도 성공. PaymentId: {}", payment.getId());

//         } catch (Exception e) {
//             log.error("결제 재시도 실패. PaymentId: {}, Error: {}", payment.getId(), e.getMessage());

//             // 실패 기록
//             createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.ABORTED);
//             retry.recordFailedAttempt(e.getMessage(), extractErrorCode(e));

//             if (retry.getStatus() == PaymentRetryEntity.RetryStatus.EXHAUSTED) {
//                 log.error("재시도 횟수 초과. PaymentId: {}", payment.getId());
//                 // 알림 발송 등 추가 처리
//                 notifyRetryExhausted(payment, retry);
//             }
//         }
//     }

//     /**
//      * 재시도 가능한 모든 결제 처리 (스케줄러용)
//      */
//     @Transactional
//     public void processRetryablePayments() {
//         List<PaymentRetryEntity> retryablePayments = paymentRetryRepository
//             .findRetryablePayments(LocalDateTime.now());

//         log.info("재시도 대상 결제 {}건 발견", retryablePayments.size());

//         retryablePayments.forEach(retry -> {
//             try {
//                 executeRetry(retry.getId());
//             } catch (Exception e) {
//                 log.error("재시도 처리 중 오류 발생. RetryId: {}, Error: {}",
//                     retry.getId(), e.getMessage(), e);
//             }
//         });
//     }

//     /**
//      * 수동으로 재시도 중단
//      */
//     @Transactional
//     public void abandonRetry(UUID retryId, String reason) {
//         PaymentRetryEntity retry = paymentRetryRepository.findById(retryId)
//             .orElseThrow(() -> new ResourceNotFoundException("재시도 항목을 찾을 수 없습니다"));

//         retry.markAsAbandoned(reason);
//         log.info("재시도 중단됨. RetryId: {}, Reason: {}", retryId, reason);
//     }

//     // === Private Helper Methods ===

//     private PaymentRetryEntity.RetryStrategy determineStrategy(String errorCode) {
//         // 에러 코드에 따라 전략 결정
//         if (errorCode != null && errorCode.startsWith("NETWORK")) {
//             return PaymentRetryEntity.RetryStrategy.EXPONENTIAL_BACKOFF;
//         }
//         return PaymentRetryEntity.RetryStrategy.LINEAR_BACKOFF;
//     }

//     private int determineMaxRetries(String errorCode) {
//         // 일시적 오류는 더 많이 재시도
//         if (errorCode != null && (errorCode.contains("TIMEOUT") || errorCode.contains("NETWORK"))) {
//             return 5;
//         }
//         // 카드 승인 실패 등은 재시도 의미 없음
//         if (errorCode != null && errorCode.contains("CARD_DECLINED")) {
//             return 0;
//         }
//         return 3; // 기본값
//     }

//     private String extractErrorCode(Exception e) {
//         // 예외에서 에러 코드 추출
//         if (e.getMessage() != null && e.getMessage().contains("code=")) {
//             return e.getMessage().split("code=")[1].split(",")[0];
//         }
//         return "UNKNOWN_ERROR";
//     }

//     private void notifyRetryExhausted(PaymentEntity payment, PaymentRetryEntity retry) {
//         // TODO: 관리자에게 알림 발송
//         log.error("재시도 횟수 초과 알림. PaymentId: {}, Attempts: {}, LastError: {}",
//             payment.getId(), retry.getAttemptCount(), retry.getLastErrorMessage());
//     }

//     private String getBillingKey() {
//         // TODO: 실제 구현
//         return "billing-key";
//     }

//     private String getCustomerKey() {
//         // TODO: 실제 구현
//         return "customer-key";
//     }
// }
