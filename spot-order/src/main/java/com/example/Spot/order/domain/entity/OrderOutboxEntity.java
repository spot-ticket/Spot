package com.example.Spot.order.domain.entity;

import jakarta.persistence.Index;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.order.domain.enums.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_order_outbox", indexes = {
        @Index(name = "idx_outobx_status_created_at", columnList = "outbox_status, created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutboxEntity extends BaseEntity {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;
    
    @Column(name = "aggregate_id", nullable = false, columnDefinition = "UUID") 
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(columnDefinition = "TEXT", nullable= false)
    private String payload;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "outbox_status", nullable = false)
    private OutboxStatus outboxStatus = OutboxStatus.INIT;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Builder
    public OrderOutboxEntity(String aggregateType, UUID aggregateId, String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.retryCount = 0;
        this.outboxStatus = OutboxStatus.INIT;
    }
    
    public void markPublished() {
        if (this.outboxStatus.canTransitionTo(OutboxStatus.PUBLISHED)) {
            this.outboxStatus = OutboxStatus.PUBLISHED;
            this.publishedAt = LocalDateTime.now();
        }
    }
    
    public void markFailed() {
        if (this.outboxStatus.canTransitionTo(OutboxStatus.FAILED)) {
            this.outboxStatus = OutboxStatus.FAILED;
        }
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
}
