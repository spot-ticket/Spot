package com.example.Spot.order.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.order.presentation.dto.response.OrderStatsResponseDto;

public interface OrderService {

    OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Integer userId);

    OrderResponseDto getOrderById(UUID orderId);
    OrderResponseDto getOrderByOrderNumber(String orderNumber);
    
    List<OrderResponseDto> getUserActiveOrders(Integer userId);

    // Chef 전용
    List<OrderResponseDto> getChefTodayOrders(Integer userId);

    // Owner 전용
    List<OrderResponseDto> getMyStoreActiveOrders(Integer userId);

    // 고객 주문 조회
    Page<OrderResponseDto> getUserOrders(
            Integer userId,
            UUID storeId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable);

    // 점주 매장 주문 조회
    Page<OrderResponseDto> getMyStoreOrders(
            Integer userId,
            Integer customerId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable);

    // 관리자 전체 주문 조회
    Page<OrderResponseDto> getAllOrders(
            UUID storeId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable);

    // 주문 상태 변경 (Owner/Chef)
    OrderResponseDto acceptOrder(UUID orderId, Integer estimatedTime);
    OrderResponseDto rejectOrder(UUID orderId, String reason);
    
    // 조리 상태 변경 (Chef)
    OrderResponseDto startCooking(UUID orderId);
    OrderResponseDto readyForPickup(UUID orderId);
    
    // 픽업 완료 (Owner)
    OrderResponseDto completeOrder(UUID orderId);
    
    // 주문 취소
    OrderResponseDto customerCancelOrder(UUID orderId, String reason);
    OrderResponseDto storeCancelOrder(UUID orderId, String reason);
    
    // 주문 취소 완료
    void completeOrderCancellation(UUID orderId);
    
    // 결제 관련 (Payment Service에서 호출)
    OrderResponseDto completePayment(UUID orderId);
    OrderResponseDto failPayment(UUID orderId);

    // 통계 조회 (Admin용 - Feign Client에서 호출)
    OrderStatsResponseDto getOrderStats();
}
