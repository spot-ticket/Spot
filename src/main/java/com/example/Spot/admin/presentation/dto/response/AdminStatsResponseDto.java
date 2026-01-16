package com.example.Spot.admin.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.AccessLevel;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserGrowthDto {
        private String date;
        private Long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OrderStatusStatsDto {
        private String status;
        private Long count;
    }
}
