package com.example.Spot.payments.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.example.Spot.global.common.BaseEntity;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Entity
@Table(name="p_payment_cancel")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancelEntity extends BaseEntity{
    
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="payment_items_id")
    private PaymentItemEntity paymentItem;

    @Column(name="cancel_idempotency", updatable=false)
    private UUID CancelIdempotency;

    @Column(nullable=false, updatable=false)
    private String reason;

    @Builder
    public PaymentCancelEntity(PaymentItemEntity paymentItem, String reason){
        this.paymentItem = paymentItem;
        this.reason = reason;
    }
}