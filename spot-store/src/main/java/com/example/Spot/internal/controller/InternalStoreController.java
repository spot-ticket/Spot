package com.example.Spot.internal.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.internal.dto.InternalStoreResponse;
import com.example.Spot.internal.dto.InternalStoreUserResponse;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.entity.StoreUserEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.domain.repository.StoreUserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalStoreController {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<InternalStoreResponse> getStoreById(@PathVariable UUID storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        return ResponseEntity.ok(InternalStoreResponse.from(store));
    }

    @GetMapping("/stores/{storeId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable UUID storeId) {
        return ResponseEntity.ok(storeRepository.existsById(storeId));
    }

    @GetMapping("/store-users/by-user")
    public ResponseEntity<InternalStoreUserResponse> getStoreUserByUserId(@RequestParam Integer userId) {
        StoreUserEntity storeUser = storeUserRepository.findFirstByUserId(userId);
        if (storeUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(InternalStoreUserResponse.from(storeUser));
    }
}
