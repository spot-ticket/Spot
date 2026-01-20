package com.example.Spot.order.domain.enums;

public enum CancelledBy {
    CUSTOMER("고객 취소"),
    STORE("매장 취소"),
    SYSTEM("시스템 자동 취소");

    private final String description;

    CancelledBy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
