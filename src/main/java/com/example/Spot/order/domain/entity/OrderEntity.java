package com.example.Spot.order.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.store.domain.entity.StoreEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Column(name = "need_disposables", nullable = false)
    private Boolean needDisposables;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cooking_started_at")
    private LocalDateTime cookingStartedAt;

    @Column(name = "cooking_completed_at")
    private LocalDateTime cookingCompletedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "estimated_time")
    private Integer estimatedTime; // 조리 예상 시간 (분)

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // 주문 취소/거절 이유

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by", length = 20)
    private CancelledBy cancelledBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems;

    @Builder
    public OrderEntity(StoreEntity store, Long userId, String orderNumber,
                       String request, Boolean needDisposables, LocalDateTime pickupTime) {
        this.store = store;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.request = request;
        this.needDisposables = needDisposables != null ? needDisposables : false;
        this.pickupTime = pickupTime;
        this.orderStatus = OrderStatus.PENDING;
        this.orderItems = new ArrayList<>();
    }

    // 주문 수락 (OWNER)
    public void acceptOrder(Integer estimatedTime) {
        this.orderStatus = OrderStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.estimatedTime = estimatedTime;
    }

    // 주문 거절 (OWNER)
    public void rejectOrder(String reason) {
        this.orderStatus = OrderStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.reason = reason;
    }

    // 주문 취소 (OWNER, CUSTOMER)
    public void cancelOrder(String reason, CancelledBy cancelledBy) {
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.reason = reason;
        this.cancelledBy = cancelledBy;
    }

    // 조리 시작 (CHEF)
    public void startCooking() {
        this.orderStatus = OrderStatus.COOKING;
        this.cookingStartedAt = LocalDateTime.now();
    }

    // 조리 완료 = 픽업 대기 (CHEF)
    public void readyForPickup() {
        this.orderStatus = OrderStatus.READY;
        this.cookingCompletedAt = LocalDateTime.now();
    }

    // 픽업 완료 (CUSTOMER)
    public void completeOrder() {
        this.orderStatus = OrderStatus.COMPLETED;
        this.pickedUpAt = LocalDateTime.now();
    }
}
