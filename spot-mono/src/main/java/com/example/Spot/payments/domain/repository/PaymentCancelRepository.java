package com.example.Spot.payments.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.payments.domain.entity.PaymentCancelEntity;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancelEntity, UUID> {
}
