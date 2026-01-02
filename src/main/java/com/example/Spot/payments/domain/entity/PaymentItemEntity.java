package com.example.Spot.payments.domain.entity;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.order.domain.entity.OrderEntity;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Builder
    public PaymentItemEntity(PaymentEntity payment, OrderEntity order) {

        if (payment == null) {
            throw new IllegalArgumentException("사전에 등록된 결제가 있어야 합니다.");
        }
        if (order == null) {
            throw new IllegalArgumentException("사전에 등록된 주문이 있어야 합니다.");
        }

        this.payment = payment;
        this.order = order;
    }
}
