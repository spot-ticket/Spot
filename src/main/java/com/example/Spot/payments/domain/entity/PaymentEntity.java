package com.example.Spot.payments.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.user.domain.entity.UserEntity;

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
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity extends BaseEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false, nullable = false, name = "user_id")
    private UserEntity user;

    @Column(updatable = false, nullable = false, length = 100)
    private String paymentTitle;

    @Column(updatable = false, nullable = false, length = 255)
    private String paymentContent;

    @Column(name = "payment_key", unique = true)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, nullable = false, name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(updatable = false, nullable = false, name = "payment_amount")
    private Long totalAmount;

    @Builder
    public PaymentEntity(UserEntity user, String title, String content,
                         PaymentMethod paymentMethod, Long totalAmount) {

        this.user = user;
        this.paymentTitle = title;
        this.paymentContent = content;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.paymentKey = null;
    }

    public void confirm(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public String getPaymentKey() {
        return this.paymentKey;
    }

    public enum PaymentMethod {
        CREDIT_CARD,         // 신용 카드
        BANK_TRANSFER        // 계좌 이체
    }
}
