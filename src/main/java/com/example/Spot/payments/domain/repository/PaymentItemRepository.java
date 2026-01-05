package com.example.Spot.payments.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentItemEntity;

public interface PaymentItemRepository extends JpaRepository<PaymentItemEntity, UUID> {

    Optional<PaymentItemEntity> findTopByPaymentOrderByCreatedAtDesc(PaymentEntity payment);

    List<PaymentItemEntity> findByPaymentOrderByCreatedAtAsc(PaymentEntity payment);

    Optional<PaymentItemEntity> findTopByPaymentAndOrderOrderByCreatedAtDesc(
        PaymentEntity payment, OrderEntity order);

    List<PaymentItemEntity> findAllByPayment(PaymentEntity payment);
}
