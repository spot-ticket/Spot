package com.example.Spot.order.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.order.application.service.OrderService;
import com.example.Spot.order.presentation.code.OrderSuccessCode;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHEF')")
public class ChefOrderController {

    private final OrderService orderService;

    @GetMapping("/chef/today")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getChefTodayOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        List<OrderResponseDto> response = orderService.getChefTodayOrders(userId);
        
        return ResponseEntity
                .status(OrderSuccessCode.ORDER_LIST_FOUND.getStatus())
                .body(ApiResponse.onSuccess(OrderSuccessCode.ORDER_LIST_FOUND, response));
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
}

