package com.example.Spot.global.feign.dto;

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

    private Long totalOrders;
    private Long totalRevenue;
    private List<OrderStatusStat> orderStatusStats;


    public static OrderStatsResponse empty() {
        return OrderStatsResponse.builder()
                .totalOrders(0L)
                .totalRevenue(0L)
                .orderStatusStats(List.of())
                .build();
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusStat {

        private String status;
        private Long count;
    }
}
