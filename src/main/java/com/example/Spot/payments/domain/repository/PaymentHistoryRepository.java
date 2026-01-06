package com.example.Spot.payments.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistoryEntity, UUID> {

    Optional<PaymentHistoryEntity> findTopByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

    List<PaymentHistoryEntity> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

    List<PaymentHistoryEntity> findAllByPaymentId(UUID paymentId);
}
