package com.example.Spot.order.domain.entity;

import com.example.Spot.store.domain.entity.StoreEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.example.Spot.store.domain.entity.StoreEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import lombok.Builder;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_order")
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Builder.Default
    @Column(name = "need_disposables", nullable = false)
    private Boolean needDisposables = false;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @Builder.Default
    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "cooking_started_at")
    private LocalDateTime cookingStartedAt;

    @Column(name = "cooking_completed_at")
    private LocalDateTime cookingCompletedAt;

    @Column(name = "estimated_time")
    private Integer estimatedTime; // 조리 예상 시간 (분)

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // 주문 취소/거절 이유

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) 
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    public enum OrderStatus {
        PENDING,              // 주문 수락 대기
        ACCEPTED,             // 주문 수락
        REJECTED,             // 주문 거절
        COOKING,              // 조리중
        READY,                // 픽업 대기
        COMPLETED,            // 픽업 완료
        CANCELLED             // 주문 취소
    }
}

