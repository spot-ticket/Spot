package com.example.Spot.global.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.global.feign.dto.StoreUserResponse;

@FeignClient(name = "spot-store", url = "${feign.store.url}")
public interface StoreClient {

    @GetMapping("/api/internal/store-users/exists")
    boolean existsByStoreIdAndUserId(
            @RequestParam("storeId") UUID storeId,
            @RequestParam("userId") Integer userId);

    @GetMapping("/api/internal/store-users/by-user")
    StoreUserResponse getStoreUserByUserId(@RequestParam("userId") Integer userId);
}
