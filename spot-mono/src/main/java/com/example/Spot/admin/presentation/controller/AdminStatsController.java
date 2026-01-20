package com.example.Spot.admin.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.admin.application.service.AdminStatsService;
import com.example.Spot.admin.presentation.dto.response.AdminStatsResponseDto;
import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminStatsResponseDto>> getStats() {
        AdminStatsResponseDto stats = adminStatsService.getStats();

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, stats));
    }
}
