package com.example.Spot.store.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.feign.dto.StorePageResponse;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.application.service.AdminStoreInternalService;
import com.example.Spot.store.presentation.dto.response.AdminStoreListResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/stores")
@PreAuthorize("hasAnyRole('MASTER','MANAGER')")
public class InternalAdminStoreController {

    private final AdminStoreInternalService adminStoreInternalService;

    @GetMapping
    public ResponseEntity<StorePageResponse<AdminStoreListResponse>> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // 너가 이미 갖고 있는 admin 목록 조회 서비스가 Page로 주면 이대로 변환
        Page<AdminStoreListResponse> stores = Page.empty(pageable);

        return ResponseEntity.ok(StorePageResponse.from(stores));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        adminStoreInternalService.deleteStore(storeId, userId);
        return ResponseEntity.noContent().build();
    }
}
