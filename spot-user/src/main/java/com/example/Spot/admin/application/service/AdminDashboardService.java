package com.example.Spot.admin.application.service;

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
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final OrderClient orderClient;
    private final StoreAdminClient storeAdminClient;

    @Cacheable(value = "admin_dashboard", key = "'dashboard'", cacheManager = "redisCacheManager")
    public AdminStatsResponseDto getDashboard() {
        Long totalUsers = 0L;
        List<AdminStatsResponseDto.UserGrowthDto> userGrowth = calculateUserGrowth(7);

        Long totalOrders = 0L;
        Long totalRevenue = 0L;
        Long totalStores = 0L;

        List<?> recentOrders = List.of();
        List<AdminStatsResponseDto.OrderStatusStatsDto> orderStatusStats = List.of();

        // 1) Users
        try {
            totalUsers = userRepository.count();
        } catch (Exception ignored) {
            totalUsers = 0L;
        }

        // 2) Orders
        try {
            OrderStatsResponse stats = orderClient.getOrderStats();
            if (stats != null) {
                totalOrders = stats.getTotalOrders() == null ? 0L : stats.getTotalOrders();
                totalRevenue = stats.getTotalRevenue() == null ? 0L : stats.getTotalRevenue();

                if (stats.getOrderStatusStats() != null) {
                    orderStatusStats = stats.getOrderStatusStats()
                            .stream()
                            .map(s -> AdminStatsResponseDto.OrderStatusStatsDto.builder()
                                    .status(s.getStatus())
                                    .count(s.getCount())
                                    .build())
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception ignored) {
            totalOrders = 0L;
            totalRevenue = 0L;
            orderStatusStats = List.of();
        }

        try {
            OrderPageResponse page = orderClient.getAllOrders(0, 10, "createdAt", "DESC");
            if (page != null && page.getContent() != null) {
                recentOrders = page.getContent();
            }
        } catch (Exception ignored) {
            recentOrders = List.of();
        }

        // 3) Stores
        try {
            totalStores = storeAdminClient.getStoreCount();
        } catch (Exception ignored) {
            totalStores = 0L;
        }

        return AdminStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .userGrowth(userGrowth)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalStores(totalStores)
                .recentOrders(recentOrders)
                .orderStats(orderStatusStats)
                .build();
    }

    private List<AdminStatsResponseDto.UserGrowthDto> calculateUserGrowth(int days) {
        List<AdminStatsResponseDto.UserGrowthDto> growth = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = now.minusDays(i).toLocalDate();

            growth.add(AdminStatsResponseDto.UserGrowthDto.builder()
                    .date(date.toString())
                    .count(0L)
                    .build());
        }

        return growth;
    }
}
