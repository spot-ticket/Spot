package com.example.Spot.admin.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.Cacheable;
import com.example.Spot.admin.presentation.dto.response.AdminStatsResponseDto;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    // private final RedissonClient redissonClient;

    @Cacheable(value = "admin_dashboard", key = "'stats'", cacheManager = "redisCacheManager")
    public AdminStatsResponseDto getStats() {

        // RLock lock = redissonClient.getLock("admin_status_lock:" + orderId);

        Long totalUsers  = userRepository.count();
        Long totalOrders = orderRepository.count();
        Long totalStores = storeRepository.count();

        List<OrderEntity> completedOrders = orderRepository.findAll();

        BigDecimal totalRevenue = completedOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(item -> item.getMenuPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pageable recentPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<OrderResponseDto> recentOrders = orderRepository.findAll(recentPageable)
                .stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());

        List<AdminStatsResponseDto.UserGrowthDto> userGrowth = calculateUserGrowth(7);

        List<AdminStatsResponseDto.OrderStatusStatsDto> orderStats = calculateOrderStatusStats();

        return AdminStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .totalStores(totalStores)
                .totalRevenue(totalRevenue)
                .recentOrders(recentOrders)
                .userGrowth(userGrowth)
                .orderStats(orderStats)
                .build();
    }

    private List<AdminStatsResponseDto.UserGrowthDto> calculateUserGrowth(int days) {
        List<AdminStatsResponseDto.UserGrowthDto> growth = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = now.minusDays(i).toLocalDate();

            // 간단히 날짜별 사용자 수를 0으로 설정 (실제 구현 시 적절한 쿼리 필요)
            Long count = 0L;
            growth.add(AdminStatsResponseDto.UserGrowthDto.builder()
                    .date(date.toString())
                    .count(count)
                    .build());
        }

        return growth;
    }

    private List<AdminStatsResponseDto.OrderStatusStatsDto> calculateOrderStatusStats() {
        List<OrderEntity> orders = orderRepository.findAll();
        Map<String, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderStatus().name(),
                        Collectors.counting()
                ));

        return statusCounts.entrySet().stream()
                .map(entry -> AdminStatsResponseDto.OrderStatusStatsDto.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
