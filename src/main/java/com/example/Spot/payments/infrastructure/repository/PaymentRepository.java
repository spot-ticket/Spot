package com.example.Spot.payments.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.payments.domain.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {

}