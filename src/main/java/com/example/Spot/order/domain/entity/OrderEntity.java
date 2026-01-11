package com.example.Spot.order.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.domain.exception.InvalidOrderStatusTransitionException;
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

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "need_disposables", nullable = false)
    private Boolean needDisposables = false;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus = OrderStatus.PAYMENT_PENDING;

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt;

    @Column(name = "payment_failed_at")
    private LocalDateTime paymentFailedAt;
    
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

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by", length = 20)
    private CancelledBy cancelledBy;

    @Column(name = "estimated_time")
    private Integer estimatedTime; // 조리 예상 시간 (분)

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // 주문 취소/거절 이유

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @Builder
    public OrderEntity(StoreEntity store, Integer userId, String orderNumber,
                       String request, boolean needDisposables, LocalDateTime pickupTime) {
        if (store == null) {
            throw new IllegalArgumentException("가게 정보는 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("주문 번호는 필수입니다.");
        }
        if (pickupTime == null) {
            throw new IllegalArgumentException("픽업 시간은 필수입니다.");
        }
        
        this.store = store;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.request = request;
        this.needDisposables = needDisposables;
        this.pickupTime = pickupTime;
    }

    public void addOrderItem(OrderItemEntity orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("주문 항목은 null일 수 없습니다.");
        }
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 결제 완료 처리
    public void completePayment() {
        validateStatusTransition(OrderStatus.PENDING);
        this.orderStatus = OrderStatus.PENDING;
        this.paymentCompletedAt = LocalDateTime.now();
    }

    // 결제 실패 처리
    public void failPayment() {
        validateStatusTransition(OrderStatus.PAYMENT_FAILED);
        this.orderStatus = OrderStatus.PAYMENT_FAILED;
        this.paymentFailedAt = LocalDateTime.now();
    }

    // 재결제 시도
    public void retryPayment() {
        validateStatusTransition(OrderStatus.PAYMENT_PENDING);
        this.orderStatus = OrderStatus.PAYMENT_PENDING;
        this.paymentFailedAt = null;
    }

    public void removeOrderItem(OrderItemEntity orderItem) {
        if (orderItem == null) {
            return;
        }
        this.orderItems.remove(orderItem);
    }

    // 주문 수락 (OWNER)
    public void acceptOrder(Integer estimatedTime) {
        validateStatusTransition(OrderStatus.ACCEPTED);
        this.orderStatus = OrderStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.estimatedTime = estimatedTime;
    }

    // 주문 거절 (OWNER)
    public void rejectOrder(String reason) {
        validateStatusTransition(OrderStatus.REJECTED);
        this.orderStatus = OrderStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.reason = reason;
    }

    // 주문 취소 (OWNER, CUSTOMER)
    public void cancelOrder(String reason, CancelledBy cancelledBy) {
        validateStatusTransition(OrderStatus.CANCELLED);
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.reason = reason;
        this.cancelledBy = cancelledBy;
    }

    // 조리 시작 (CHEF)
    public void startCooking() {
        validateStatusTransition(OrderStatus.COOKING);
        this.orderStatus = OrderStatus.COOKING;
        this.cookingStartedAt = LocalDateTime.now();
    }

    // 조리 완료 = 픽업 대기 (CHEF)
    public void readyForPickup() {
        validateStatusTransition(OrderStatus.READY);
        this.orderStatus = OrderStatus.READY;
        this.cookingCompletedAt = LocalDateTime.now();
    }

    // 픽업 완료 (OWNER)
    public void completeOrder() {
        validateStatusTransition(OrderStatus.COMPLETED);
        this.orderStatus = OrderStatus.COMPLETED;
        this.pickedUpAt = LocalDateTime.now();
    }

    // 상태 전환 검증
    private void validateStatusTransition(OrderStatus newStatus) {
        if (!this.orderStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusTransitionException(this.orderStatus, newStatus);
        }
    }
}
