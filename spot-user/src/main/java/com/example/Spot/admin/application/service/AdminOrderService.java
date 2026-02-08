package com.example.Spot.admin.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.Spot.global.feign.OrderClient;
import com.example.Spot.global.feign.StoreAdminClient;
import com.example.Spot.global.feign.dto.OrderPageResponse;
import com.example.Spot.global.feign.dto.OrderResponse;
import com.example.Spot.global.feign.dto.OrderStatsResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderClient orderClient;
    private final StoreAdminClient storeAdminClient;

    public OrderPageResponse getAllOrders(Pageable pageable, String sortBy, String direction) {
        OrderPageResponse page = orderClient.getAllOrders(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction
        );

        if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
            return page;
        }

        List<OrderResponse> orders = page.getContent();

        List<UUID> storeIds = orders.stream()
                .map(OrderResponse::getStoreId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<UUID, String> storeNameMap = Map.of();
        if (!storeIds.isEmpty()) {
            Map<UUID, String> resp = storeAdminClient.getStoreNames(storeIds);
            if (resp != null) {
                storeNameMap = resp;
            }
        }

        Map<UUID, String> finalStoreNameMap = storeNameMap;
        List<OrderResponse> mapped = orders.stream()
                .map(o -> OrderResponse.builder()
                        .orderId(o.getOrderId())
                        .userId(o.getUserId())
                        .storeId(o.getStoreId())
                        .orderNumber(o.getOrderNumber())
                        .pickupTime(o.getPickupTime())
                        .createdAt(o.getCreatedAt())
                        .status(o.getStatus())
                        .totalAmount(o.getTotalAmount())
                        .storeName(finalStoreNameMap.getOrDefault(o.getStoreId(), null))
                        .build())
                .toList();

        return OrderPageResponse.builder()
                .content(mapped)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .number(page.getNumber())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public OrderStatsResponse getOrderStats() {
        return orderClient.getOrderStats();
    }

    public long getOrderCount() {
        return orderClient.getOrderCount();
    }
}
