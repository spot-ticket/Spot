package com.example.Spot.payments.application.service;

import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.entity.PaymentRetryEntity;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private PaymentHistoryRepository paymentHistoryRepository;
    private PaymentRetryRepository paymentRetryRepository;
    private PaymentKeyRepository paymentKeyRepository;

    @Transactional
    public PaymentHistoryEntity recordPaymentReady(UUID paymentId) {

        return createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.READY);
    }

    @Transactional
    public PaymentHistoryEntity recordCancelProgress(UUID paymentId) {
        PaymentHistoryEntity latestItem =
            paymentHistoryRepository
                .findTopByPaymentIdOrderByCreatedAtDesc(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 결제 이력을 찾을 수 없습니다."));

        if (latestItem.getStatus() != PaymentHistoryEntity.PaymentStatus.DONE) {
            throw new IllegalStateException("[PaymentService] 결제 완료된 내역만 취소 가능합니다.");
        }

        return createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.CANCELLED_IN_PROGRESS);
    }

    @Transactional
    public PaymentHistoryEntity recordCancelSuccess(UUID paymentId) {
        return createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.CANCELLED);
    }

    @Transactional
    public void recordPaymentProgress(UUID paymentId) {
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
    public void recordFailure(UUID paymentId, Exception e) {
        PaymentHistoryEntity paymentHistory =
            createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.ABORTED);
        createPaymentRetry(paymentId, paymentHistory.getId(), e.getMessage());
    }

    @Transactional
    public PaymentHistoryEntity recordPaymentSuccess(UUID paymentId, String paymentKey) {
        PaymentHistoryEntity paymentHistory =
            createPaymentHistory(paymentId, PaymentHistoryEntity.PaymentStatus.DONE);
        createPaymentKey(paymentId, paymentKey, LocalDateTime.now());

        return paymentHistory;
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
}
