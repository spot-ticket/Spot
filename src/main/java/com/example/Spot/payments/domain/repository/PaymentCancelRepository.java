package com.example.Spot.payments.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.payments.domain.entity.PaymentCancelEntity;
import com.example.Spot.payments.domain.entity.PaymentItemEntity;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancelEntity, UUID> {

    Optional<PaymentCancelEntity> findByPaymentItem(PaymentItemEntity paymentItem);

    List<PaymentCancelEntity> findAllByPaymentItem(PaymentItemEntity paymentItem);
}
