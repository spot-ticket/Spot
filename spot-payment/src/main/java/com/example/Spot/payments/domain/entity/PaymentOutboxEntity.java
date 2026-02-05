package com.example.Spot.payments.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_payment_outbox", indexes = {
        @Index(name = "idx_payment_outbox_status_next_attempt", columnList = "outbox_status, next_attempt_at")
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

    @Builder
    public PaymentOutboxEntity(String aggregateType, UUID aggregateId, String eventKey, String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }
}
