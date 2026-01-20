package com.example.Spot.sales.presentation.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.sales.application.service.SalesService;
import com.example.Spot.sales.presentation.dto.response.DailySalesResponse;
import com.example.Spot.sales.presentation.dto.response.PopularMenuResponse;
import com.example.Spot.sales.presentation.dto.response.SalesSummaryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stores/{storeId}/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OWNER', 'CHEF', 'MANAGER', 'MASTER')")
public class SalesController {

    private final SalesService salesService;

    /**
     * 매출 요약 조회
     * OWNER/CHEF는 본인 가게만, 관리자는 모든 가게 조회 가능
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponse>> getSalesSummary(
            @PathVariable UUID storeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 기본값: 최근 30일
        LocalDateTime start = (startDate != null)
                ? startDate.atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = (endDate != null)
                ? endDate.atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        SalesSummaryResponse summary = salesService.getSalesSummary(
                storeId,
                start,
                end,
                principal.getUserId(),
                principal.getUserRole()
        );

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, summary)
        );
    }

    /**
     * 일별 매출 조회
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailySalesResponse>>> getDailySales(
            @PathVariable UUID storeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 기본값: 최근 30일
        LocalDateTime start = (startDate != null)
                ? startDate.atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = (endDate != null)
                ? endDate.atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        List<DailySalesResponse> dailySales = salesService.getDailySales(
                storeId,
                start,
                end,
                principal.getUserId(),
                principal.getUserRole()
        );

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, dailySales)
        );
    }

    /**
     * 인기 메뉴 조회
     */
    @GetMapping("/popular-menus")
    public ResponseEntity<ApiResponse<List<PopularMenuResponse>>> getPopularMenus(
            @PathVariable UUID storeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 기본값: 최근 30일
        LocalDateTime start = (startDate != null)
                ? startDate.atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = (endDate != null)
                ? endDate.atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        List<PopularMenuResponse> popularMenus = salesService.getPopularMenus(
                storeId,
                start,
                end,
                principal.getUserId(),
                principal.getUserRole(),
                limit
        );

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, popularMenus)
        );
    }
}
