package com.example.Spot.order.presentation.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.example.Spot.order.presentation.dto.request.OrderRejectRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerOrderController {

    private final OrderService orderService;

    @GetMapping("/my-store")
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getMyStoreOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Integer userId = userDetails.getUserId();
        LocalDateTime dateTime = date != null ? date.atStartOfDay() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderResponseDto> response = orderService.getMyStoreOrdersWithPagination(
                userId, customerId, dateTime, status, pageable);

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

    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<OrderResponseDto>> completeOrder(@PathVariable UUID orderId) {
        OrderResponseDto response = orderService.completeOrder(orderId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_COMPLETED, response));
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

