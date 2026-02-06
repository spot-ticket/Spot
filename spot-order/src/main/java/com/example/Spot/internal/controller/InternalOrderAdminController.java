package com.example.Spot.internal.controller;

import java.util.UUID;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.feign.dto.OrderPageResponse;
import com.example.Spot.global.feign.dto.OrderResponse;
import com.example.Spot.global.feign.dto.OrderStatsResponse;
import com.example.Spot.order.application.service.InternalOrderAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/orders")
@PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
public class InternalOrderAdminController {

    private final InternalOrderAdminService adminOrderInternalService;

    @GetMapping
    public ResponseEntity<OrderPageResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<OrderResponse> orders = adminOrderInternalService.getAllOrders(pageable, sortBy, direction);
        return ResponseEntity.ok(OrderPageResponse.from(orders));

    }

    @GetMapping("/stats")
    public ResponseEntity<OrderStatsResponse> getOrderStats() {
        return ResponseEntity.ok(adminOrderInternalService.getOrderStats());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getOrderCount() {
        return ResponseEntity.ok(adminOrderInternalService.getOrderCount());
    }

    @GetMapping("/{orderId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(adminOrderInternalService.existsById(orderId));
    }
}
