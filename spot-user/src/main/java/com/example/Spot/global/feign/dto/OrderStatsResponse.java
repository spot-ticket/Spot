package com.example.Spot.global.feign.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsResponse {

    private long totalOrders;
    private BigDecimal totalRevenue;
    private List<OrderStatusStats> orderStatusStats;

    public static OrderStatsResponse empty() {
        return OrderStatsResponse.builder()
                .totalOrders(0L)
                .totalRevenue(BigDecimal.ZERO)
                .orderStatusStats(List.of())
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusStats {
        private String status;
        private long count;
    }
}
