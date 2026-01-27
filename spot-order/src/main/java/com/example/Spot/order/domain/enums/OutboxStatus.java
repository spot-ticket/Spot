package com.example.Spot.order.domain.enums;

public enum OutboxStatus {
    INIT("발행 대기"),
    PUBLISHED("발행 완료"),
    FAILED("발행 실패");
    
    private final String description;
    
    OutboxStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    // 상태 전환 검증
    public boolean canTransitionTo(OutboxStatus newStatus) {
        return switch (this) {
            case INIT -> newStatus == PUBLISHED || newStatus == FAILED;
            case FAILED -> newStatus == PUBLISHED;
            default -> false;
        };
    }
}
    
