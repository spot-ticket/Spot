package com.example.Spot.payments.domain.entity;

import com.example.Spot.order.domain.entity.OrderEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Table(name="p_payment_item")
@Getter
@EntityListeners(AuditingListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentItemEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="payment_id")
    private PaymentEntity payment;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id")
    private OrderEntity order;

    @CreatedDate
    @Column(name="created_at")
    private LocalDateTime createdAt;
}