package com.example.Spot.payments.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Table(name="p_payment_item")
@Getter
@EntityListeners(AuditingListener.class)
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

    @CreateDate
    @Column(name="created_at")
    private LocaDateTime createdAt;
}