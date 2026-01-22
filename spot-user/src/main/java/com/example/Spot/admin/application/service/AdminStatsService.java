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
import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.dto.OrderPageResponse;
import com.example.Spot.global.feign.dto.OrderStatsResponse;
import com.example.Spot.global.feign.dto.StorePageResponse;
import com.example.Spot.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final OrderClient orderClient;
    private final StoreClient storeClient;

    @Cacheable(value = "admin_dashboard", key = "'stats'", cacheManager = "redisCacheManager")
    public AdminStatsResponseDto getStats() {

        // User 서비스 내부 조회 (직접 접근 가능)
        Long totalUsers = userRepository.count();

        // Order 서비스 호출 (Feign)
        OrderStatsResponse orderStats = orderClient.getOrderStats();
        OrderPageResponse recentOrders = orderClient.getAllOrders(0, 10, "createdAt", "DESC");

        // Store 서비스 호출 (Feign)
        StorePageResponse storeResponse = storeClient.getAllStores(0, 1);

        List<AdminStatsResponseDto.UserGrowthDto> userGrowth = calculateUserGrowth(7);

        List<AdminStatsResponseDto.OrderStatusStatsDto> orderStatusStats = orderStats.getOrderStatusStats()
                .stream()
                .map(stat -> AdminStatsResponseDto.OrderStatusStatsDto.builder()
                        .status(stat.getStatus())
                        .count(stat.getCount())
                        .build())
                .collect(Collectors.toList());

        return AdminStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .totalOrders(orderStats.getTotalOrders())
                .totalStores(storeResponse.totalElements())
                .totalRevenue(orderStats.getTotalRevenue())
                .recentOrders(recentOrders.getContent())
                .userGrowth(userGrowth)
                .orderStats(orderStatusStats)
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
