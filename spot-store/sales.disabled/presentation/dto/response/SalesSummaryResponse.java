package com.example.Spot.sales.presentation.dto.response;

import java.time.LocalDateTime;

public record SalesSummaryResponse(
        Long totalRevenue,          // 총 매출
        Long totalOrders,            // 총 주문 수
        Long completedOrders,        // 완료된 주문 수
        Long cancelledOrders,        // 취소된 주문 수
        Double averageOrderAmount,   // 평균 주문 금액
        LocalDateTime periodStart,   // 조회 기간 시작
        LocalDateTime periodEnd      // 조회 기간 종료
) {
}
