package com.example.Spot.global.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.global.feign.dto.StorePageResponse;
import com.example.Spot.global.feign.dto.StoreResponse;

@FeignClient(name = "spot-store", contextId = "storeClient", url = "${feign.store.url}")
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
