package com.example.Spot.store.presentation.dto.response;

import java.util.UUID;

import com.example.Spot.store.domain.entity.StoreEntity;

public record AdminStoreListResponse(
        UUID storeId,
        String name,
        String status
) {

    public static AdminStoreListResponse from(StoreEntity store) {
        return new AdminStoreListResponse(store.getId(), store.getName(), store.getStatus());
    }
}
