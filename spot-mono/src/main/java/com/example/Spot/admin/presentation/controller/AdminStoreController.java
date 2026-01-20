package com.example.Spot.admin.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.admin.application.service.AdminStoreService;
import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
public class AdminStoreController {

    private final AdminStoreService adminStoreService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StoreListResponse>>> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<StoreListResponse> stores = adminStoreService.getAllStores(pageable);

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, stores));
    }

    @PatchMapping("/{storeId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveStore(@PathVariable UUID storeId) {
        adminStoreService.approveStore(storeId);

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
        @PathVariable UUID storeId,
        @AuthenticationPrincipal CustomUserDetails principal) {

        // To Do: 관리자 페이지에서 삭제를 했을 때, 삭제된 것들에 대한 조치가 없음. 해결할 것.
        adminStoreService.deleteStore(storeId, principal.getUserId());

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null));
    }
}
