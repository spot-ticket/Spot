package com.example.Spot.payments.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.order.domain.entity.OrderEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_payment_item")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentItemEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_status")
    private PaymentStatus paymentStatus;

    @Builder
    public PaymentItemEntity(PaymentEntity payment, OrderEntity order, PaymentStatus status) {

        if (payment == null) {
            throw new IllegalArgumentException("사전에 등록된 결제가 있어야 합니다.");
        }
        if (order == null) {
            throw new IllegalArgumentException("사전에 등록된 주문이 있어야 합니다.");
        }

        this.payment = payment;
        this.order = order;
        this.paymentStatus = status;
    }

    public PaymentStatus getPaymentStatus() {
        return this.paymentStatus;
    }
    
    public enum PaymentStatus {
        READY,               // 결제 준비
        IN_PROGRESS,         // 소유자가 맞는지 인증을 마친 상태
        WAITING_FOR_DEPOSIT, // 가상계좌 결제 흐름에만 있는 상태
        DONE,                // 인증된 결제수단으로 요청한 결제가 승인된 상태
        CANCELLED,           // 결제 취소
        PARTIAL_CANCELD,     // 승인된 결제가 부분 취소된 상태
        ABORTED,             // 결제 승인이 실패한 상태
        EXPIRED              // 결제 유효 시간 30분이 지나 거래가 취소 상태. IN_PROGRESS 상태에서 결제 승인 API를 호출하지 않으면 EXPIRED가 됨.
    }
}
