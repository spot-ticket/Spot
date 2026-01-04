package com.example.Spot.order.presentation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.order.application.service.OrderService;
import com.example.Spot.order.presentation.code.OrderSuccessCode;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderCommonController {

    private final OrderService orderService;

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
}

