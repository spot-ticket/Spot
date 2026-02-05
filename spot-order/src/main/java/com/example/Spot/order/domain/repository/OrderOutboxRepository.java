package com.example.Spot.order.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.order.domain.entity.OrderOutboxEntity;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, UUID> {
    @Query("SELECT o FROM OrderOutboxEntity  o " + 
    "WHERE o.outboxStatus = 'INIT' " +
    "AND o.nextAttemptAt <= :now " + 
    "ORDER By o.createdAt ASC")
    List<OrderOutboxEntity> findTop10ReadyTopublish(@Param("now") LocalDateTime now, Pageable pageable);
}
