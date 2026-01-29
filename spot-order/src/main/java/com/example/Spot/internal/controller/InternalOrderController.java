package com.example.Spot.internal.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.internal.dto.InternalOrderResponse;
import com.example.Spot.order.domain.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/{orderId}")
    public ResponseEntity<InternalOrderResponse> getOrderById(@PathVariable UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> ResponseEntity.ok(InternalOrderResponse.from(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderRepository.existsById(orderId));
    }
}
