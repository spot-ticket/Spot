package com.example.Spot.global.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.example.Spot.global.feign.dto.StorePageResponse;
import com.example.Spot.global.feign.dto.StoreResponse;

@FeignClient(name = "store-service", url = "${feign.store.url}")
public interface StoreClient {

    @GetMapping("/api/stores")
    StorePageResponse getAllStores(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @GetMapping("/api/stores/{storeId}")
    StoreResponse getStoreById(@PathVariable("storeId") UUID storeId);

    @PatchMapping("/api/stores/{storeId}/status")
    void updateStoreStatus(
            @PathVariable("storeId") UUID storeId,
            @RequestParam("status") String status
    );

    @DeleteMapping("/api/stores/{storeId}")
    void deleteStore(@PathVariable("storeId") UUID storeId);
}
