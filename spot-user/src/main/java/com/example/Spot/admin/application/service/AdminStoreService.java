package com.example.Spot.admin.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.dto.StorePageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private final StoreClient storeClient;

    public StorePageResponse getAllStores(int page, int size) {
        return storeClient.getAllStores(page, size);
    }

    public void approveStore(UUID storeId) {
        storeClient.updateStoreStatus(storeId, "APPROVED");
    }

    public void deleteStore(UUID storeId) {
        storeClient.deleteStore(storeId);
    }
}
