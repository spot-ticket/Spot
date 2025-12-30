package com.example.Spot.payments.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import org.springframework.data.annotation.createdDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name="p_payment_cancel")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class PaymentCancelEntity {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @Column(name="cancel_idempotency", updatable=false)
    private UUID CancelIdempotency;

    @Column(nullable=false, updatable=false)
    private String reason;

    @CreatedDate
    @Column(name="created_at")
    private LocalDateTime createdAt;
}