package com.example.Spot.store.presentation.controller.internal;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Spot.store.application.service.AdminStoreInternalService;
import com.example.Spot.store.presentation.dto.request.StoreStatusUpdateRequest;
import com.example.Spot.store.presentation.dto.response.AdminStoreListResponse;
import com.example.Spot.store.presentation.dto.response.StorePageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/stores")
@PreAuthorize("hasAnyRole('MASTER','MANAGER')")
public class InternalAdminStoreController {

    private final AdminStoreInternalService adminStoreInternalService;

    @GetMapping
    public ResponseEntity<StorePageResponse> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminStoreListResponse> stores = adminStoreInternalService.getAllStores(pageable);
        return ResponseEntity.ok(StorePageResponse.from(stores));
    }

    @PatchMapping("/{storeId}/status")
    public ResponseEntity<Void> updateStoreStatus(
            @PathVariable UUID storeId,
            @RequestBody StoreStatusUpdateRequest request
    ) {
        adminStoreInternalService.updateStoreStatus(storeId, request.status());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID storeId) {
        adminStoreInternalService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }
}
