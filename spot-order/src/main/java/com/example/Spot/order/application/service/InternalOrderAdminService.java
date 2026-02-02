package com.example.Spot.order.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.Spot.global.feign.dto.OrderResponse;
import com.example.Spot.global.feign.dto.OrderStatsResponse;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.domain.repository.OrderItemOptionRepository;
import com.example.Spot.order.domain.repository.OrderRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalOrderAdminService {

    private final OrderRepository orderRepository;

    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.PAYMENT_PENDING,
            OrderStatus.PENDING,
            OrderStatus.ACCEPTED,
            OrderStatus.COOKING,
            OrderStatus.READY
    );
    private final OrderItemOptionRepository orderItemOptionRepository;


    public Page<OrderResponse> getAllOrders(Pageable pageable, String sortBy, String direction) {
        Sort sort = Sort.by(parseDirection(direction), sortBy);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return orderRepository.findAll(sortedPageable)
                .map(order -> OrderResponse.builder()
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .storeId(order.getStoreId())
                        .orderNumber(order.getOrderNumber())
                        .pickupTime(order.getPickupTime())
                        .createdAt(order.getCreatedAt())
                        .build());
    }



    public OrderStatsResponse getOrderStats() {
        long totalOrders = orderRepository.count();

        BigDecimal totalRevenueDecimal = orderItemOptionRepository.sumTotalRevenue();
        Long totalRevenue = totalRevenueDecimal != null
                ? totalRevenueDecimal.longValue()
                : 0L;

        List<Object[]> statusResults = orderRepository.countGroupByStatus();

        List<OrderStatsResponse.OrderStatusStat> orderStatusStats =
                statusResults.stream()
                        .map(r -> OrderStatsResponse.OrderStatusStat.builder()
                                .status(((OrderStatus) r[0]).name())
                                .count((Long) r[1])
                                .build())
                        .toList();

        return OrderStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .orderStatusStats(orderStatusStats)
                .build();
    }


    public long getOrderCount() {
        return orderRepository.count();
    }

    public boolean existsById(UUID orderId) {
        return orderRepository.existsById(orderId);
    }
    private Sort.Direction parseDirection(String direction) {
        if (direction == null) {
            return Sort.Direction.DESC;
        }
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            return Sort.Direction.DESC;
        }
    }
}
