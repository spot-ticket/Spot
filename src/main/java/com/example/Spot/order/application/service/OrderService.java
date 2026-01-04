package com.example.Spot.order.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

public interface OrderService {

    OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Integer userId);

    OrderResponseDto getOrderById(UUID orderId);
    OrderResponseDto getOrderByOrderNumber(String orderNumber);
    
    List<OrderResponseDto> getUserOrders(Integer userId);
    List<OrderResponseDto> getUserOrdersByStatus(Integer userId, OrderStatus status);
    List<OrderResponseDto> getUserActiveOrders(Integer userId);
    List<OrderResponseDto> getUserOrdersByDateRange(Integer userId, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderResponseDto> getUserOrdersByFilters(Integer userId, UUID storeId, LocalDateTime date, OrderStatus status);
    
    List<OrderResponseDto> getStoreOrders(UUID storeId);
    List<OrderResponseDto> getStoreOrdersByStatus(UUID storeId, OrderStatus status);
    List<OrderResponseDto> getStoreActiveOrders(UUID storeId);
    List<OrderResponseDto> getStoreOrdersByDateRange(UUID storeId, LocalDateTime startDate, LocalDateTime endDate);
    List<OrderResponseDto> getStoreOrdersByFilters(UUID storeId, Integer customerId, LocalDateTime date, OrderStatus status);
    
    // Chef 전용
    List<OrderResponseDto> getChefTodayOrders(Integer userId);
    
    // Owner 전용
    List<OrderResponseDto> getMyStoreOrders(Integer userId, Integer customerId, LocalDateTime date, OrderStatus status);
    List<OrderResponseDto> getMyStoreActiveOrders(Integer userId);
    
    // 주문 상태 변경 (Owner/Chef)
    OrderResponseDto acceptOrder(UUID orderId, Integer estimatedTime);
    OrderResponseDto rejectOrder(UUID orderId, String reason);
    
    // 조리 상태 변경 (Chef)
    OrderResponseDto startCooking(UUID orderId);
    OrderResponseDto readyForPickup(UUID orderId);
    
    // 주문 완료 (Customer)
    OrderResponseDto completeOrder(UUID orderId);
    
    // 주문 취소
    OrderResponseDto customerCancelOrder(UUID orderId, String reason);
    OrderResponseDto storeCancelOrder(UUID orderId, String reason);
    
    // 결제 관련 (Payment Service에서 호출)
    OrderResponseDto completePayment(UUID orderId);
    OrderResponseDto failPayment(UUID orderId);
}

