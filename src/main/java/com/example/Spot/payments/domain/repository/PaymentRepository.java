package com.example.Spot.payments.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.payments.domain.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

  // orderId로 진행 중이거나 완료된 결제가 있는지 확인
  @Query(
      """
        SELECT p FROM PaymentEntity p
        WHERE p.orderId = :orderId
        AND EXISTS (
            SELECT 1 FROM PaymentHistoryEntity h
            WHERE h.paymentId = p.id
            AND h.paymentStatus IN ('READY', 'IN_PROGRESS', 'DONE')
            AND h.id = (
                SELECT h2.id FROM PaymentHistoryEntity h2
                WHERE h2.paymentId = p.id
                ORDER BY h2.createdAt DESC
                LIMIT 1
            )
        )
        """)
  Optional<PaymentEntity> findActivePaymentByOrderId(@Param("orderId") UUID orderId);

  // 결제 목록 조회 - 최신 상태와 함께 한 번의 쿼리로 조회
  @Query(
      """
        SELECT p.id, p.paymentTitle, p.paymentContent, p.paymentMethod, p.totalAmount,
               h.paymentStatus, p.createdAt
        FROM PaymentEntity p
        LEFT JOIN PaymentHistoryEntity h ON h.paymentId = p.id
        WHERE h.id = (
            SELECT h2.id FROM PaymentHistoryEntity h2
            WHERE h2.paymentId = p.id
            ORDER BY h2.createdAt DESC
            LIMIT 1
        )
        ORDER BY p.createdAt DESC
        """)
  List<Object[]> findAllPaymentsWithLatestStatus();

  // 단일 결제 상세 조회 - 최신 상태와 함께
  @Query(
      """
        SELECT p.id, p.paymentTitle, p.paymentContent, p.paymentMethod, p.totalAmount,
               h.paymentStatus, p.createdAt
        FROM PaymentEntity p
        LEFT JOIN PaymentHistoryEntity h ON h.paymentId = p.id
        WHERE p.id = :paymentId
        AND h.id = (
            SELECT h2.id FROM PaymentHistoryEntity h2
            WHERE h2.paymentId = p.id
            ORDER BY h2.createdAt DESC
            LIMIT 1
        )
        """)
  List<Object[]> findPaymentWithLatestStatus(@Param("paymentId") UUID paymentId);
}
