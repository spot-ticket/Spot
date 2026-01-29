package com.example.Spot.global.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.global.feign.dto.StoreResponse;
import com.example.Spot.global.feign.dto.StoreUserResponse;

@FeignClient(name = "spot-store", url = "${feign.store.url}")
public interface StoreClient {

    @GetMapping("/api/stores/{storeId}")
    StoreResponse getStoreById(@PathVariable("storeId") UUID storeId);

    @GetMapping("/api/stores/{storeId}/exists")
    boolean existsById(@PathVariable("storeId") UUID storeId);

    @GetMapping("/api/internal/store-users/by-user")
    StoreUserResponse getStoreUserByUserId(@RequestParam("userId") Integer userId);
}
