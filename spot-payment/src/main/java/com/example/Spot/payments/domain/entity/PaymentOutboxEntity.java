package com.example.Spot.payments.domain.entity;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.payments.domain.enums.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "p_payment_outbox", indexes = {
        @Index(name = "idx_outbox_status_next_attempt", columnList = "outbox_status, next_attempt_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOutboxEntity extends BaseEntity {
    
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

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbox_status", nullable = false)
    private OutboxStatus outboxStatus = OutboxStatus.INIT;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Builder
    public PaymentOutboxEntity(String aggregateType, UUID aggregateId, String eventKey, String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.retryCount = 0;
        this.outboxStatus = OutboxStatus.INIT;
        this.nextAttemptAt = LocalDateTime.now();
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

    public void retryFailed() {
        this.retryCount++;
        long delaySeconds = (long) Math.min(Math.pow(2, this.retryCount), 3600);
        this.nextAttemptAt = LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
