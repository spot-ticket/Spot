package com.example.Spot.payments.domain.entity;

import com.example.Spot.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

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
