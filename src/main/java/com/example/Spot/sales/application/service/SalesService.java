package com.example.Spot.sales.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.sales.presentation.dto.response.DailySalesResponse;
import com.example.Spot.sales.presentation.dto.response.PopularMenuResponse;
import com.example.Spot.sales.presentation.dto.response.SalesSummaryResponse;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.Role;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StoreRepository storeRepository;

    /**
     * 가게의 매출 요약 조회
     * OWNER는 본인 가게만, 관리자는 모든 가게 조회 가능
     */
    public SalesSummaryResponse getSalesSummary(
            UUID storeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer userId,
            Role userRole) {

        // 권한 확인
        validateStoreAccess(storeId, userId, userRole);

        // 기간 내 주문 조회
        List<OrderEntity> orders = orderRepository.findByStoreIdAndDateRange(
                storeId,
                startDate,
                endDate
        );

        // 완료된 주문만 매출 계산
        List<OrderEntity> completedOrders = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .toList();

        // 총 매출 계산 (결제 금액 기준)
        Long totalRevenue = completedOrders.stream()
                .mapToLong(order -> {
                    try {
                        PaymentEntity payment = paymentRepository.findActivePaymentByOrderId(order.getId())
                                .orElse(null);
                        return payment != null ? payment.getTotalAmount() : 0L;
                    } catch (Exception e) {
                        return 0L;
                    }
                })
                .sum();

        // 취소된 주문 수
        Long cancelledOrders = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.CANCELLED ||
                        order.getOrderStatus() == OrderStatus.REJECTED)
                .count();

        // 평균 주문 금액
        Double averageOrderAmount = completedOrders.isEmpty() ? 0.0 :
                (double) totalRevenue / completedOrders.size();

        return new SalesSummaryResponse(
                totalRevenue,
                (long) orders.size(),
                (long) completedOrders.size(),
                cancelledOrders,
                Math.round(averageOrderAmount * 100.0) / 100.0,
                startDate,
                endDate
        );
    }

    /**
     * 일별 매출 조회
     */
    public List<DailySalesResponse> getDailySales(
            UUID storeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer userId,
            Role userRole) {

        validateStoreAccess(storeId, userId, userRole);

        List<OrderEntity> orders = orderRepository.findByStoreIdAndDateRange(
                storeId,
                startDate,
                endDate
        );

        // 완료된 주문만 필터링
        List<OrderEntity> completedOrders = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .toList();

        // 날짜별로 그룹화
        Map<LocalDate, List<OrderEntity>> ordersByDate = completedOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate()
                ));

        // 일별 매출 계산
        List<DailySalesResponse> dailySales = new ArrayList<>();
        LocalDate currentDate = startDate.toLocalDate();
        LocalDate endDateLocal = endDate.toLocalDate();

        while (!currentDate.isAfter(endDateLocal)) {
            List<OrderEntity> dayOrders = ordersByDate.getOrDefault(currentDate, List.of());

            Long dayRevenue = dayOrders.stream()
                    .mapToLong(order -> {
                        try {
                            PaymentEntity payment = paymentRepository.findActivePaymentByOrderId(order.getId())
                                    .orElse(null);
                            return payment != null ? payment.getTotalAmount() : 0L;
                        } catch (Exception e) {
                            return 0L;
                        }
                    })
                    .sum();

            dailySales.add(new DailySalesResponse(
                    currentDate,
                    dayRevenue,
                    (long) dayOrders.size()
            ));

            currentDate = currentDate.plusDays(1);
        }

        return dailySales;
    }

    /**
     * 인기 메뉴 조회
     */
    public List<PopularMenuResponse> getPopularMenus(
            UUID storeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer userId,
            Role userRole,
            int limit) {

        validateStoreAccess(storeId, userId, userRole);

        List<OrderEntity> orders = orderRepository.findByStoreIdAndDateRange(
                storeId,
                startDate,
                endDate
        );

        // 완료된 주문의 메뉴별 집계
        Map<UUID, MenuSalesData> menuSalesMap = new HashMap<>();

        orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getOrderItems().stream())
                .forEach(item -> {
                    UUID menuId = item.getMenu().getId();
                    String menuName = item.getMenu().getName();
                    Long itemRevenue = item.getMenuPrice().longValue() * item.getQuantity();

                    menuSalesMap.computeIfAbsent(menuId, k -> new MenuSalesData(menuId, menuName))
                            .addSale(item.getQuantity(), itemRevenue);
                });

        // 주문 수 기준 정렬하여 상위 N개 반환
        return menuSalesMap.values().stream()
                .sorted((a, b) -> Long.compare(b.orderCount, a.orderCount))
                .limit(limit)
                .map(data -> new PopularMenuResponse(
                        data.menuId,
                        data.menuName,
                        data.orderCount,
                        data.totalRevenue
                ))
                .toList();
    }

    /**
     * 가게 접근 권한 확인
     */
    private void validateStoreAccess(UUID storeId, Integer userId, Role userRole) {
        // 관리자는 모든 가게 접근 가능
        if (userRole == Role.MASTER || userRole == Role.MANAGER) {
            return;
        }

        // OWNER는 본인 가게만 접근 가능
        if (userRole == Role.OWNER || userRole == Role.CHEF) {
            StoreEntity store = storeRepository.findByIdWithDetailsForOwner(storeId)
                    .orElseThrow(() -> new EntityNotFoundException("가게를 찾을 수 없습니다."));

            boolean isOwner = store.getUsers().stream()
                    .anyMatch(su -> su.getUser().getId().equals(userId));

            if (!isOwner) {
                throw new AccessDeniedException("해당 가게의 매출 정보를 조회할 권한이 없습니다.");
            }
        } else {
            throw new AccessDeniedException("매출 정보를 조회할 권한이 없습니다.");
        }
    }

    /**
     * 메뉴별 매출 데이터 저장용 내부 클래스
     */
    private static class MenuSalesData {
        UUID menuId;
        String menuName;
        Long orderCount = 0L;
        Long totalRevenue = 0L;

        MenuSalesData(UUID menuId, String menuName) {
            this.menuId = menuId;
            this.menuName = menuName;
        }

        void addSale(int quantity, Long revenue) {
            this.orderCount += quantity;
            this.totalRevenue += revenue;
        }
    }
}
