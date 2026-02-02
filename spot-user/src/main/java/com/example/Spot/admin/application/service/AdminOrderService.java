package com.example.Spot.admin.application.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.Spot.global.feign.OrderClient;
import com.example.Spot.global.feign.dto.OrderStatsResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderClient orderClient;

    public Object getAllOrders(Pageable pageable, String sortBy, String direction) {
        return orderClient.getAllOrders(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction
        );
    }

    public OrderStatsResponse getOrderStats() {
        return orderClient.getOrderStats();
    }

    public long getOrderCount() {
        return orderClient.getOrderCount();
    }

//    public void updateOrderStatus(java.util.UUID orderId, Object request) {
//        // 필요하면 internal api 추가 후 호출로 연결
//        throw new UnsupportedOperationException("Not implemented");
//    }
}
