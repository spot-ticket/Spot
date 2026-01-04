package com.example.Spot.order.domain.exception;

import com.example.Spot.order.domain.enums.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    
    public InvalidOrderStatusTransitionException(OrderStatus currentStatus, OrderStatus newStatus) {
        super(String.format("주문 상태를 %s에서 %s로 변경할 수 없습니다.", 
            currentStatus.getDescription(), 
            newStatus.getDescription()));
    }
}

