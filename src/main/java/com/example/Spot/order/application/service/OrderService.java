package com.example.Spot.order.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

public interface OrderService {

    OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Long userId);

    OrderResponseDto getOrderById(UUID orderId);
    OrderResponseDto getOrderByOrderNumber(String orderNumber);
    
    List<OrderResponseDto> getUserOrders(Long userId);
    List<OrderResponseDto> getUserOrdersByStatus(Long userId, OrderStatus status);
    List<OrderResponseDto> getUserActiveOrders(Long userId);
    
    List<OrderResponseDto> getStoreOrders(UUID storeId);
    List<OrderResponseDto> getStoreOrdersByStatus(UUID storeId, OrderStatus status);
    List<OrderResponseDto> getStoreActiveOrders(UUID storeId);
    
    // 주문 상태 변경 (Owner/Chef)
    OrderResponseDto acceptOrder(UUID orderId, Integer estimatedTime);
    OrderResponseDto rejectOrder(UUID orderId, String reason);
    
    // 조리 상태 변경 (Chef)
    OrderResponseDto startCooking(UUID orderId);
    OrderResponseDto readyForPickup(UUID orderId);
    
    // 주문 완료 (Customer)
    OrderResponseDto completeOrder(UUID orderId);
    
    // 주문 취소 (Customer/Owner)
    OrderResponseDto cancelOrder(UUID orderId, String reason, CancelledBy cancelledBy);
    
    // 결제 관련 (Payment Service에서 호출)
    OrderResponseDto completePayment(UUID orderId);
    OrderResponseDto failPayment(UUID orderId);
}

