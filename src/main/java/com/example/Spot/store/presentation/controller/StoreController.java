package com.example.Spot.store.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.store.application.service.StoreService;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;
import com.example.Spot.user.domain.entity.UserEntity;

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
            @AuthenticationPrincipal(expression = "userEntity") UserEntity currentUser
    ) {
        UUID storeId = storeService.createStore(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(storeId);
    }

    // 2. 매장 상세 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponse> getStoreDetails(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal(expression = "userEntity") UserEntity currentUser) {

        return ResponseEntity.ok(storeService.getStoreDetails(storeId, currentUser));
    }
    
    // 3. 매장 전체 조회
    @GetMapping
    public ResponseEntity<List<StoreListResponse>> getAllStores(
            @AuthenticationPrincipal(expression = "userEntity") UserEntity currentUser) {
        
        return ResponseEntity.ok(storeService.getAllStores(currentUser));
    }
    
    // 4. 매장 정보 수정
    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @AuthenticationPrincipal(expression = "userEntity") UserEntity currentUser
    ) {
        storeService.updateStore(storeId, request, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    // 5. 매장 삭제 (Soft Delete)
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal(expression = "userEntity") UserEntity currentUser
    ) {
        storeService.deleteStore(storeId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
