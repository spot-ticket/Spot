package com.example.Spot.admin.application.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.Spot.admin.presentation.dto.AdminStoreListResponseDto;
import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.dto.StorePageResponse;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private final StoreClient storeClient;

    public StorePageResponse<AdminStoreListResponseDto> getAllStores(Pageable pageable) {
        return storeClient.getAllStores(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }
    
    public void approveStore(UUID storeId) {
        storeClient.updateStoreStatus(storeId, "APPROVED");
    }

    public void deleteStore(UUID storeId, Integer userId) {
        storeClient.deleteStore(storeId);
    }
    
}
