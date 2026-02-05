package com.example.Spot.order.domain.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.order.domain.entity.OrderOutboxEntity;
import org.springframework.transaction.annotation.Transactional;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, UUID> {
    
    @Transactional
    @Modifying
    @Query("DELETE FROM OrderOutboxEntity o WHERE o.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
