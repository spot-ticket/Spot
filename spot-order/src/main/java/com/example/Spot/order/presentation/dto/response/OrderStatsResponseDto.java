package com.example.Spot.order.presentation.dto.response;

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
public class OrderStatsResponseDto {

    private long totalOrders;
    private BigDecimal totalRevenue;
    private List<OrderStatusStats> orderStatusStats;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusStats {
        private String status;
        private long count;
    }
}
