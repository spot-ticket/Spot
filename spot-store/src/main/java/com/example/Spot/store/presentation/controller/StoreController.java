package com.example.Spot.store.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.application.service.StoreService;
import com.example.Spot.store.domain.StoreStatus;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUserUpdateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;
import com.example.Spot.store.presentation.swagger.StoreApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController implements StoreApi {
    
    private final StoreService storeService;

//    @Override
//    @PostMapping
//    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
//    public ResponseEntity<UUID> createStore(
//            @Valid @RequestBody StoreCreateRequest request,
//            @AuthenticationPrincipal Integer userId
//    ) {
//        UUID storeId = storeService.createStore(request, userId);
//        return ResponseEntity.status(HttpStatus.CREATED).body(storeId);
//    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<UUID> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        UUID storeId = storeService.createStore(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(storeId);
    }



    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('OWNER','CHEF')")
    public ResponseEntity<java.util.List<StoreListResponse>> getMyStores(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        return ResponseEntity.ok(storeService.getMyStores(userId));
    }

    @Override
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponse> getStoreDetails(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal != null ? principal.getUserId() : null;
        boolean isAdmin = principal != null && principal.getUserEntity() != null
                && (principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MASTER
                || principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MANAGER);
        return ResponseEntity.ok(storeService.getStoreDetails(storeId, userId, isAdmin));
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<StoreListResponse>> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        boolean isAdmin = principal != null && principal.getUserEntity() != null
                && (principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MASTER
                || principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MANAGER);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(storeService.getAllStores(isAdmin, pageable));
    }
    
    @Override
    @PatchMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        storeService.updateStore(storeId, request, userId);
        return ResponseEntity.noContent().build();
    }
    
    @Override
    @PatchMapping("/{storeId}/staff")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> updateStoreStaff(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        storeService.updateStoreStaff(storeId, request, userId);
        return ResponseEntity.noContent().build();
    }
    
    @Override
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    public ResponseEntity<Void> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        boolean isAdmin = principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MASTER
                || principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MANAGER;
        storeService.deleteStore(storeId, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{storeId}/status")
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    public ResponseEntity<Void> updateStoreStatus(
            @PathVariable UUID storeId,
            @RequestParam StoreStatus status,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        storeService.updateStoreStatus(storeId, status, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<StoreListResponse>> searchStores(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        boolean isAdmin = principal != null && principal.getUserEntity() != null
                && (principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MASTER
                || principal.getUserEntity().getRole() == com.example.Spot.user.domain.Role.MANAGER);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(storeService.searchStoresByName(keyword, isAdmin, pageable));
    }
}
