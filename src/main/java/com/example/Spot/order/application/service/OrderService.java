package com.example.Spot.order.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

public interface OrderService {

    OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Integer userId);

    OrderResponseDto getOrderById(UUID orderId);
    OrderResponseDto getOrderByOrderNumber(String orderNumber);
    
    List<OrderResponseDto> getUserOrders(Integer userId);
    List<OrderResponseDto> getUserActiveOrders(Integer userId);
    List<OrderResponseDto> getUserOrdersByFilters(Integer userId, UUID storeId, LocalDateTime date, OrderStatus status);

    // Chef 전용
    List<OrderResponseDto> getChefTodayOrders(Integer userId);

    // Owner 전용
    List<OrderResponseDto> getMyStoreOrders(Integer userId, Integer customerId, LocalDateTime date, OrderStatus status);
    List<OrderResponseDto> getMyStoreActiveOrders(Integer userId);

    // MASTER/MANAGER 전용 - 전체 매장 주문 조회
    List<OrderResponseDto> getAllOrders(UUID storeId, LocalDateTime date, OrderStatus status);

    // ========== 페이지네이션 ==========

    // 고객 주문 조회
    Page<OrderResponseDto> getUserOrdersWithPagination(
            Integer userId,
            UUID storeId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable);

    // 점주 매장 주문 조회
    Page<OrderResponseDto> getMyStoreOrdersWithPagination(
            Integer userId,
            Integer customerId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable);

    // 관리자 전체 주문 조회
    Page<OrderResponseDto> getAllOrdersWithPagination(
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
    
    // 결제 관련 (Payment Service에서 호출)
    OrderResponseDto completePayment(UUID orderId);
    OrderResponseDto failPayment(UUID orderId);
}
