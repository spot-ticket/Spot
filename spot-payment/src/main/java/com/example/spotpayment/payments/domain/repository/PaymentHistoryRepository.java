package com.example.Spot.payments.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity.PaymentStatus;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistoryEntity, UUID> {

  Optional<PaymentHistoryEntity> findTopByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

  List<PaymentHistoryEntity> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

  List<PaymentHistoryEntity> findAllByPaymentId(UUID paymentId);

  // 전체 취소 내역 조회 - Payment 정보와 함께 한 번에 조회
  @Query(
      """
        SELECT h.id, h.paymentId, p.totalAmount, h.createdAt
        FROM PaymentHistoryEntity h
        JOIN PaymentEntity p ON p.id = h.paymentId
        WHERE h.paymentStatus = :status
        ORDER BY h.createdAt DESC
        """)
  List<Object[]> findAllByStatusWithPayment(@Param("status") PaymentStatus status);

  // 특정 결제의 취소 내역 조회
  @Query(
      """
        SELECT h.id, h.paymentId, p.totalAmount, h.createdAt
        FROM PaymentHistoryEntity h
        JOIN PaymentEntity p ON p.id = h.paymentId
        WHERE h.paymentId = :paymentId
        AND h.paymentStatus IN (:statuses)
        ORDER BY h.createdAt DESC
        """)
  List<Object[]> findByPaymentIdAndStatusesWithPayment(
      @Param("paymentId") UUID paymentId, @Param("statuses") List<PaymentStatus> statuses);

  // 일정 시간 동안 READY 또는 IN_PROGRESS 상태로 멈춰있는 결제 조회 (타임아웃 처리용)
  @Query(
      """
        SELECT h FROM PaymentHistoryEntity h
        WHERE h.paymentId IN (
            SELECT sub.paymentId FROM PaymentHistoryEntity sub
            WHERE sub.id = (
                SELECT MAX(sub2.id) FROM PaymentHistoryEntity sub2
                WHERE sub2.paymentId = sub.paymentId
            )
            GROUP BY sub.paymentId
            HAVING MAX(sub.paymentStatus) IN ('READY', 'IN_PROGRESS')
        )
        AND h.createdAt = (
            SELECT MAX(h2.createdAt) FROM PaymentHistoryEntity h2
            WHERE h2.paymentId = h.paymentId
        )
        AND h.paymentStatus IN ('READY', 'IN_PROGRESS')
        AND h.createdAt < :timeoutThreshold
        """)
  List<PaymentHistoryEntity> findStalePayments(@Param("timeoutThreshold") java.time.LocalDateTime timeoutThreshold);
}
