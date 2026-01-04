package com.example.Spot.order.presentation.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.order.application.service.OrderService;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.presentation.code.OrderSuccessCode;
import com.example.Spot.order.presentation.dto.request.OrderAcceptRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderCancelRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderRejectRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @Valid @RequestBody OrderCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        OrderResponseDto response = orderService.createOrder(requestDto, userId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_CREATED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_CREATED, response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(@PathVariable UUID orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_FOUND, response));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderByNumber(@PathVariable String orderNumber) {
        OrderResponseDto response = orderService.getOrderByOrderNumber(orderNumber);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_FOUND, response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) OrderStatus status) {
        
        Integer userId = userDetails.getUserId();
        List<OrderResponseDto> response;
        
        LocalDateTime dateTime = date != null ? date.atStartOfDay() : null;
        
        if (storeId != null || dateTime != null || status != null) {
            response = orderService.getUserOrdersByFilters(userId, storeId, dateTime, status);
        } else {
            response = orderService.getUserOrders(userId);
        }
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_LIST_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_LIST_FOUND, response));
    }

    @GetMapping("/my/active")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyActiveOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        List<OrderResponseDto> response = orderService.getUserActiveOrders(userId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_LIST_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_LIST_FOUND, response));
    }

    @GetMapping("/my-store")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyStoreOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) OrderStatus status) {
        
        Integer userId = userDetails.getUserId();
        LocalDateTime dateTime = date != null ? date.atStartOfDay() : null;
        
        List<OrderResponseDto> response = orderService.getMyStoreOrders(userId, customerId, dateTime, status);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_LIST_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_LIST_FOUND, response));
    }

    @GetMapping("/my-store/active")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyStoreActiveOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        List<OrderResponseDto> response = orderService.getMyStoreActiveOrders(userId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_LIST_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_LIST_FOUND, response));
    }

    @GetMapping("/chef/today")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getChefTodayOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        List<OrderResponseDto> response = orderService.getChefTodayOrders(userId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_LIST_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_LIST_FOUND, response));
    }

    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<ApiResponse<OrderResponseDto>> acceptOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderAcceptRequestDto requestDto) {
        
        OrderResponseDto response = orderService.acceptOrder(orderId, requestDto.getEstimatedTime());
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_ACCEPTED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_ACCEPTED, response));
    }

    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<OrderResponseDto>> rejectOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderRejectRequestDto requestDto) {
        
        OrderResponseDto response = orderService.rejectOrder(orderId, requestDto.getReason());
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_REJECTED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_REJECTED, response));
    }

    @PatchMapping("/{orderId}/start-cooking")
    public ResponseEntity<ApiResponse<OrderResponseDto>> startCooking(@PathVariable UUID orderId) {
        OrderResponseDto response = orderService.startCooking(orderId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_COOKING_STARTED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_COOKING_STARTED, response));
    }

    @PatchMapping("/{orderId}/ready")
    public ResponseEntity<ApiResponse<OrderResponseDto>> readyForPickup(@PathVariable UUID orderId) {
        OrderResponseDto response = orderService.readyForPickup(orderId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_READY.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_READY, response));
    }

    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<OrderResponseDto>> completeOrder(@PathVariable UUID orderId) {
        OrderResponseDto response = orderService.completeOrder(orderId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_COMPLETED, response));
    }

    @PatchMapping("/{orderId}/customer-cancel")
    public ResponseEntity<ApiResponse<OrderResponseDto>> customerCancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderCancelRequestDto requestDto) {
        
        OrderResponseDto response = orderService.customerCancelOrder(orderId, requestDto.getReason());
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_CANCELLED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_CANCELLED, response));
    }

    @PatchMapping("/{orderId}/store-cancel")
    public ResponseEntity<ApiResponse<OrderResponseDto>> storeCancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderCancelRequestDto requestDto) {
        
        OrderResponseDto response = orderService.storeCancelOrder(orderId, requestDto.getReason());
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_CANCELLED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_CANCELLED, response));
    }
}
