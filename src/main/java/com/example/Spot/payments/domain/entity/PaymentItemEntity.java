package com.example.Spot.payments.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.time.LocalDateTime;
import com.example.Spot.order.domain.entity.OrderEntity;
import java.util.UUID;

@Entity
@Table(name="p_payment_item")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentItemEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
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