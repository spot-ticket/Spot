package com.example.Spot.admin.application.service;
//
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.Spot.admin.presentation.dto.response.AdminStatsResponseDto;
import com.example.Spot.global.feign.OrderClient;
import com.example.Spot.global.feign.StoreAdminClient;
import com.example.Spot.global.feign.dto.OrderPageResponse;
import com.example.Spot.global.feign.dto.OrderStatsResponse;
import com.example.Spot.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final OrderClient orderClient;
    private final StoreAdminClient storeAdminClient;

    @Cacheable(value = "admin_dashboard", key = "'user_stats'", cacheManager = "redisCacheManager")
    public AdminStatsResponseDto getUserStats() {
        Long totalUsers = userRepository.count();
        List<AdminStatsResponseDto.UserGrowthDto> userGrowth = calculateUserGrowth(7);

        return AdminStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .userGrowth(userGrowth)
                .build();
    }

    @Cacheable(value = "admin_dashboard", key = "'order_stats'", cacheManager = "redisCacheManager")
    public AdminStatsResponseDto getOrderStats() {
        OrderStatsResponse orderStats = orderClient.getOrderStats();
        OrderPageResponse recentOrders = orderClient.getAllOrders(0, 10, "createdAt", "DESC");

        List<AdminStatsResponseDto.OrderStatusStatsDto> orderStatusStats =
                (orderStats.getOrderStatusStats() == null ? List.<OrderStatsResponse.OrderStatusStats>of() : orderStats.getOrderStatusStats())
                        .stream()
                        .map(stat -> AdminStatsResponseDto.OrderStatusStatsDto.builder()
                                .status(stat.getStatus())
                                .count(stat.getCount())
                                .build())
                        .collect(Collectors.toList());

        return AdminStatsResponseDto.builder()
                .totalOrders(orderStats.getTotalOrders())
                .totalRevenue(orderStats.getTotalRevenue())
                .recentOrders(recentOrders.getContent())
                .orderStats(orderStatusStats)
                .build();
    }

    @Cacheable(value = "admin_dashboard", key = "'store_stats'", cacheManager = "redisCacheManager")
    public AdminStatsResponseDto getStoreStats() {
        long totalStores = storeAdminClient.getStoreCount();

        return AdminStatsResponseDto.builder()
                .totalStores(totalStores)
                .build();
    }


    private List<AdminStatsResponseDto.UserGrowthDto> calculateUserGrowth(int days) {
        List<AdminStatsResponseDto.UserGrowthDto> growth = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = now.minusDays(i).toLocalDate();
            Long count = 0L;

            growth.add(AdminStatsResponseDto.UserGrowthDto.builder()
                    .date(date.toString())
                    .count(count)
                    .build());
        }

        return growth;
    }
}
