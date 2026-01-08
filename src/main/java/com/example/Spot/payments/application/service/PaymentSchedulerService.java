package com.example.Spot.payments.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;
import com.example.Spot.payments.domain.entity.PaymentRetryEntity;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSchedulerService {

    private final PaymentRetryRepository paymentRetryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentKeyRepository paymentKeyRepository;
    private final TossPaymentClient tossPaymentClient;

    @Value("${toss.payments.billingKey}")
    private String billingKey;

    @Value("${toss.payments.customerKey}")
    private String customerKey;

    @Value("${toss.payments.timeout}")
    private Integer timeout;

    @Value("${payment.timeout.minutes:30}")
    private Integer paymentTimeoutMinutes;

    @Scheduled(fixedRate = 60000) // 1분
    @Transactional
    public void retryAbortedPayments() {
        log.info("결제 재시도 스케줄러 시작");

        List<PaymentRetryEntity> retryablePayments = paymentRetryRepository
                .findRetryablePayments(LocalDateTime.now());

        log.info("재시도 대상 결제 {}건 발견", retryablePayments.size());

        for (PaymentRetryEntity retry : retryablePayments) {
            try {
                executeRetry(retry);
            } catch (Exception e) {
                log.error("재시도 처리 중 오류 발생. RetryId: {}, Error: {}",
                        retry.getId(), e.getMessage(), e);
            }
        }

        log.info("결제 재시도 스케줄러 완료");
    }

    @Scheduled(fixedRate = 300000) //READY, IN_PROGRESS -> ABORTED, 5분
    @Transactional
    public void handleStalePayments() {
        log.info("결제 타임아웃 처리 스케줄러 시작");

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
        List<PaymentHistoryEntity> stalePayments = paymentHistoryRepository.findStalePayments(timeoutThreshold);

        log.info("타임아웃 처리 대상 결제 {}건 발견", stalePayments.size());

        for (PaymentHistoryEntity staleHistory : stalePayments) {
            try {
                abortStalePayment(staleHistory);
            } catch (Exception e) {
                log.error("타임아웃 처리 중 오류 발생. PaymentId: {}, Error: {}",
                        staleHistory.getPaymentId(), e.getMessage(), e);
            }
        }

        log.info("결제 타임아웃 처리 스케줄러 완료");
    }

    private void executeRetry(PaymentRetryEntity retry) {
        UUID paymentId = retry.getPaymentId();

        log.info("결제 재시도 시작. PaymentId: {}, Attempt: {}/{}",
                paymentId, retry.getAttemptCount() + 1, retry.getMaxRetryCount());

        retry.markAsInProgress();
        paymentRetryRepository.save(retry);

        createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.IN_PROGRESS);

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("결제를 찾을 수 없습니다. PaymentId: " + paymentId));

        try {
            TossPaymentResponse response = tossPaymentClient.requestBillingPayment(
                    billingKey,
                    payment.getTotalAmount(),
                    paymentId.toString(),
                    payment.getPaymentTitle(),
                    customerKey,
                    timeout);

            createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.DONE);
            createPaymentKey(paymentId, response.getPaymentKey(), LocalDateTime.now());
            retry.markAsSucceeded();
            paymentRetryRepository.save(retry);

            log.info("결제 재시도 성공. PaymentId: {}", paymentId);

        } catch (Exception e) {
            log.error("결제 재시도 실패. PaymentId: {}, Error: {}", paymentId, e.getMessage());

            createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.ABORTED);
            retry.recordFailedAttempt(e.getMessage(), extractErrorCode(e));
            paymentRetryRepository.save(retry);

            if (retry.getStatus() == PaymentRetryEntity.RetryStatus.EXHAUSTED) {
                log.error("재시도 횟수 초과. PaymentId: {}, 총 시도 횟수: {}",
                        paymentId, retry.getAttemptCount());
            }
        }
    }

    private void abortStalePayment(PaymentHistoryEntity staleHistory) {
        UUID paymentId = staleHistory.getPaymentId();

        log.info("타임아웃으로 인한 결제 중단. PaymentId: {}, 상태: {}, 생성시간: {}",
                paymentId, staleHistory.getStatus(), staleHistory.getCreatedAt());

        PaymentHistoryEntity abortedHistory = createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.ABORTED);

        PaymentRetryEntity retry = PaymentRetryEntity.builder()
                .paymentId(paymentId)
                .failedPaymentHistoryId(abortedHistory.getId())
                .maxRetryCount(10)
                .strategy(PaymentRetryEntity.RetryStrategy.EXPONENTIAL_BACKOFF)
                .nextRetryAt(LocalDateTime.now().plusMinutes(5))
                .build();

        paymentRetryRepository.save(retry);

        log.info("타임아웃 결제에 대한 재시도 엔트리 생성 완료. PaymentId: {}", paymentId);
    }

    private PaymentHistoryEntity createPaymentHistory(UUID paymentId, PaymentHistoryEntity.PaymentStatus status) {
        PaymentHistoryEntity history = PaymentHistoryEntity.builder()
                .paymentId(paymentId)
                .status(status)
                .build();
        return paymentHistoryRepository.save(history);
    }

    private PaymentKeyEntity createPaymentKey(UUID paymentId, String paymentKey, LocalDateTime confirmedAt) {
        PaymentKeyEntity paymentKeyEntity = PaymentKeyEntity.builder()
                .paymentId(paymentId)
                .paymentKey(paymentKey)
                .confirmedAt(confirmedAt)
                .build();
        return paymentKeyRepository.save(paymentKeyEntity);
    }

    private String extractErrorCode(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("code=")) {
            return e.getMessage().split("code=")[1].split(",")[0];
        }
        return "UNKNOWN_ERROR";
    }
}
