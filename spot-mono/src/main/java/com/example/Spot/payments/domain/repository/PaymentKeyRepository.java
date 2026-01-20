package com.example.Spot.payments.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.payments.domain.entity.PaymentKeyEntity;

public interface PaymentKeyRepository extends JpaRepository<PaymentKeyEntity, UUID> {

  Optional<PaymentKeyEntity> findByPaymentId(UUID paymentId);
}
