package com.example.Spot.admin.presentation.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.admin.application.service.AdminOrderService;
import com.example.Spot.global.feign.dto.OrderStatsResponse;
import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Object orders = adminOrderService.getAllOrders(pageable, sortBy, direction.name());

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, orders));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> getOrderStats() {
        OrderStatsResponse stats = adminOrderService.getOrderStats();

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, stats));
    }
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getOrderCount() {
        long count = adminOrderService.getOrderCount();

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, count));
    }


}
