package com.example.Spot.order.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.order.domain.entity.OrderOutboxEntity;
import com.example.Spot.order.domain.enums.OutboxStatus;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, UUID> {
    List<OrderOutboxEntity> findTop10ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus status);
}
