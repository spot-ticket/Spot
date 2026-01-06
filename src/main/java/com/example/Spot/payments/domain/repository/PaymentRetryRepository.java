package com.example.Spot.payments.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.payments.domain.entity.PaymentRetryEntity;

public interface PaymentRetryRepository extends JpaRepository<PaymentRetryEntity, UUID> {

  // 재시도 가능한 항목 조회 (스케줄러용)
  @Query(
      """
        SELECT pr FROM PaymentRetryEntity pr
        WHERE pr.status = 'PENDING'
        AND pr.nextRetryAt <= :now
        AND pr.attemptCount < pr.maxRetryCount
        ORDER BY pr.nextRetryAt ASC
        """)
  List<PaymentRetryEntity> findRetryablePayments(@Param("now") LocalDateTime now);

  // 특정 결제의 재시도 내역 조회
  Optional<PaymentRetryEntity> findByPaymentIdAndStatus(
      UUID paymentId, PaymentRetryEntity.RetryStatus status);

  // 특정 결제의 모든 재시도 내역
  List<PaymentRetryEntity> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

  // 재시도 실패 통계
  @Query(
      """
        SELECT COUNT(pr) FROM PaymentRetryEntity pr
        WHERE pr.status = 'EXHAUSTED'
        AND pr.createdAt >= :startDate
        """)
  Long countExhaustedRetries(@Param("startDate") LocalDateTime startDate);
}
