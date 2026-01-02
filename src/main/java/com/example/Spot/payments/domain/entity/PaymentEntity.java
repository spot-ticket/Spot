package com.example.Spot.payments.domain.entity;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false, nullable = false, name = "user_id")
    private UserEntity user;

    @Column(updatable = false, nullable = false, length = 100)
    private String title;

    @Column(updatable = false, nullable = false, length = 255)
    private String content;

    @Column(updatable = false, nullable = false, name = "idempotency_key")
    private UUID idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, nullable = false, name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(updatable = false, nullable = false, name = "payment_amount")
    private Long paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_status")
    private PaymentStatus paymentStatus;

    @LastModifiedDate
    @Column(nullable = false, name = "update_at")
    private LocalDateTime updatedAt;

    @Builder
    public PaymentEntity (String title, String content, UUID idempotencyKey, 
                          PaymentMethod paymentMethod, Long paymentAmount, PaymentStatus paymentStatus) {

        this.title = title;
        this.content = content;
        this.idempotencyKey = idempotencyKey;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = (paymentStatus != null) ? paymentStatus : PaymentStatus.READY;
    }

    public enum PaymentMethod {
        CREDIT_CARD,        // 신용 카드
        BANK_TRANSFER        // 계좌 이체
    }

    public enum PaymentStatus {
        READY,              // 결제 준비
        SUCCESS,            // 결제 성공
        FAILED,             // 결제 실패
        CANCELLED           // 결제 취소
    }

    public void updateStatus(PaymentStatus status) {

        if (this.paymentStatus == PaymentStatus.SUCCESS
            || this.paymentStatus == PaymentStatus.CANCELLED
            || this.paymentStatus == PaymentStatus.FAILED) {
            throw new IllegalStateException("이미 완료된 결제 상태는 변경할 수 없습니다.");
        }

        this.paymentStatus = status;
    }
}
