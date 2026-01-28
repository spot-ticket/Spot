package com.example.Spot.payments.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.payments.domain.entity.PaymentOutboxEntity;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEntity, UUID> {
    @Query("SELECT p FROM PaymentOutboxEntity p " +
    "WHERE p.outboxStatus = 'INIT' " +
    "AND p.nextAttemptAt <= :now " +
    "ORDER BY p.createdAt ASC")
    List<PaymentOutboxEntity> findTop10ReadyTopublish(@Param("noew")LocalDateTime now, Pageable pageable);
}
