package com.example.Spot.order.domain.enums;

public enum OrderStatus {
    PENDING("주문 수락 대기"),
    ACCEPTED("주문 수락"),
    REJECTED("주문 거절"),
    COOKING("조리중"),
    READY("픽업 대기"),
    COMPLETED("픽업 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // 상태 전환 가능 여부 검증
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == ACCEPTED || newStatus == REJECTED || newStatus == CANCELLED;
            case ACCEPTED -> newStatus == COOKING || newStatus == CANCELLED;
            case COOKING -> newStatus == READY || newStatus == CANCELLED;
            case READY -> newStatus == COMPLETED;
            default -> false;
        };
    }
}