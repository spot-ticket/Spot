package com.example.Spot.store.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.store.application.service.StoreService;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUserUpdateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    
    private final StoreService storeService;

    // 1. 매장 생성
    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<UUID> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal Integer userId
    ) {
        UUID storeId = storeService.createStore(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(storeId);
    }

    // 2. 매장 상세 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponse> getStoreDetails(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal Integer userId) {

        return ResponseEntity.ok(storeService.getStoreDetails(storeId, userId));
    }
    
    // 3. 매장 전체 조회
    @GetMapping
    public ResponseEntity<List<StoreListResponse>> getAllStores(
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(storeService.getAllStores(userId));
    }
    
    // 4. 매장 기본 정보 수정
    @PatchMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @AuthenticationPrincipal Integer userId
    ) {
        storeService.updateStore(storeId, request, userId);
        return ResponseEntity.noContent().build();
    }
    
    // 5. 매장 직원 정보 수정
    @PatchMapping("/{storeId}/staff")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> updateStoreStaff(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUserUpdateRequest request,
            @AuthenticationPrincipal Integer userId
    ) {
        storeService.updateStoreStaff(storeId, request, userId);
        return ResponseEntity.noContent().build();
    }
    
    // 6. 매장 삭제 (Soft Delete)
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal Integer userId
    ) {
        storeService.deleteStore(storeId, userId);
        return ResponseEntity.noContent().build();
    }
    
    // 7. 매장 이름으로 검색
    @GetMapping("/search")
    public ResponseEntity<List<StoreListResponse>> searchStores(
            @RequestParam String keyword,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(storeService.searchStoresByName(keyword, userId));
    }
}
