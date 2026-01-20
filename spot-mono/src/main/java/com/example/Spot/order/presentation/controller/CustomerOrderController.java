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
import com.example.Spot.order.presentation.dto.request.OrderCancelRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.order.presentation.swagger.CustomerOrderApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController implements CustomerOrderApi {

    private final OrderService orderService;

    @Override
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

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Integer userId = userDetails.getUserId();
        LocalDateTime dateTime = date != null ? date.atStartOfDay() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderResponseDto> response = orderService.getUserOrdersWithPagination(
                userId, storeId, dateTime, status, pageable);

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

    @PatchMapping("/{orderId}/customer-cancel")
    public ResponseEntity<ApiResponse<OrderResponseDto>> customerCancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderCancelRequestDto requestDto) {
        
        OrderResponseDto response = orderService.customerCancelOrder(orderId, requestDto.getReason());
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_CANCELLED.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_CANCELLED, response));
    }
}

