package com.example.Spot.payments.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.UUID;

@Entity
@Table(name="p_payment_cancel")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancelEntity {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="payment_items_id")
    private PaymentItemEntity paymentItem;

    @Column(name="cancel_idempotency", updatable=false)
    private UUID CancelIdempotency;

    @Column(nullable=false, updatable=false)
    private String reason;

    @CreatedDate
    @Column(name="created_at")
    private LocalDateTime createdAt;
}