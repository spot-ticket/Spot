package com.example.Spot.payments.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.Spot.user.domain.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable=false, nullable=false, name="user_id")
    private UserEntity user;

    @Column(updatable=false, nullable=false, length=25)
    private String title;

    @Column(updatable=false, nullable=false, length=150)
    private String content;

    @Column(updatable=false, nullable=false, name="idempotency_key")
    private UUID idempotencyKey;

    @Column(updatable=false, nullable=false, name="payment_method")
    private String paymentMethod;

    @Column(updatable=false, nullable=false, name="payment_amount")
    private Long paymentAmount;

    // 처음에는 READY, 결제가 완료되면 SUCCESS, 실패하면 FAIL ?
    @Column(updatable=false, nullable=false, name="payment_status")
    private String paymentStatus;

    @CreatedDate
    @Column(updatable=false, name="created_at")
    private LocalDateTime createdAt;
}