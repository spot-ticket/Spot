package com.example.Spot.payments.domain.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.Spot.global.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_payment_cancel")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancelEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_items_id")
    private PaymentItemEntity paymentItem;

    @Column(name = "cancel_idempotency", updatable = false)
    private UUID cancelIdempotency;

    @Column(nullable = false, updatable = false)
    private String reason;

    @Builder
    public PaymentCancelEntity(PaymentItemEntity paymentItem, String reason, UUID cancelIdempotency) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("취소 사유는 필수입니다.");
        }
        if (paymentItem == null) {
            throw new IllegalArgumentException("취소 대상 아이템은 필수입니다.");
        }

        this.paymentItem = paymentItem;
        this.reason = reason;
        this.cancelIdempotency = cancelIdempotency;
    }
}
