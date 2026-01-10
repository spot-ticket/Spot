package com.example.Spot.admin.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminStatsResponseDto {

    private Long totalUsers;
    private Long totalOrders;
    private Long totalStores;
    private BigDecimal totalRevenue;
    private List<OrderResponseDto> recentOrders;
    private List<UserGrowthDto> userGrowth;
    private List<OrderStatusStatsDto> orderStats;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserGrowthDto {
        private String date;
        private Long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderStatusStatsDto {
        private String status;
        private Long count;
    }
}
