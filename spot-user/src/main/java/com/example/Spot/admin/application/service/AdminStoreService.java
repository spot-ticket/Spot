package com.example.Spot.admin.application.service;

import java.util.UUID;


import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import com.example.Spot.admin.presentation.dto.AdminStoreListResponseDto;
import com.example.Spot.global.feign.StoreAdminClient;
import com.example.Spot.global.feign.dto.StorePageResponse;


import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private final StoreAdminClient storeAdminClient;


    public StorePageResponse<AdminStoreListResponseDto> getAllStores(Pageable pageable) {
        return storeAdminClient.getAllStores(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                "name",
                "ASC"
        );
    }

    public long getStoreCount() {
        return storeAdminClient.getStoreCount();
    }


    public void approveStore(UUID storeId) {
        storeAdminClient.updateStoreStatus(storeId, "APPROVED");
    }

    public void deleteStore(UUID storeId, Integer userId) {
        storeAdminClient.deleteStore(storeId);
    }


}
