package com.example.Spot.global.feign;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.Spot.global.feign.dto.StoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.admin.presentation.dto.response.AdminStoreListResponseDto;
import com.example.Spot.global.feign.dto.StorePageResponse;

@FeignClient(name = "spot-store", contextId = "storeAdminClient", url = "${feign.store.url}")
public interface StoreAdminClient {

    @GetMapping("/api/internal/admin/stores")
    StorePageResponse<StoreResponse> getAllStores(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("direction") String direction
    );

    @PatchMapping("/api/internal/admin/stores/{storeId}/approve")
    void approveStore(@PathVariable("storeId") UUID storeId);

    @DeleteMapping("/api/internal/admin/stores/{storeId}")
    void deleteStore(@PathVariable("storeId") UUID storeId);



    @GetMapping("/api/internal/admin/stores/count")
    long getStoreCount();

    @PatchMapping("/api/internal/admin/stores/{storeId}/status")
    void updateStoreStatus(
            @PathVariable("storeId") UUID storeId,
            @RequestParam("status") String status
    );

    @GetMapping("/api/internal/admin/stores/names")
    Map<UUID, String> getStoreNames(@RequestParam("ids") List<UUID> ids);



}
