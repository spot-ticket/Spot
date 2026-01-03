package com.example.Spot.payments.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentItemEntity;
import com.example.Spot.order.domain.entity.OrderEntity;

public interface PaymentItemRepository extends JpaRepository<PaymentItemEntity, UUID> {

    // 특정 payment의 최신 상태 조회
    Optional<PaymentItemEntity> findTopByPaymentOrderByCreatedAtDesc(PaymentEntity payment);

    // 특정 payment의 모든 히스토리 조회
    List<PaymentItemEntity> findByPaymentOrderByCreatedAtAsc(PaymentEntity payment);

    // 특정 payment-order 쌍의 최신 상태 조회
    Optional<PaymentItemEntity> findTopByPaymentAndOrderOrderByCreatedAtDesc(
        PaymentEntity payment, OrderEntity order);
}
