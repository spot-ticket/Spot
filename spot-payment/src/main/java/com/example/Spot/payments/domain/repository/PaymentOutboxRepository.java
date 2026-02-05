package com.example.Spot.payments.domain.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.payments.domain.entity.PaymentOutboxEntity;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEntity, UUID> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PaymentOutboxEntity p WHERE p.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
