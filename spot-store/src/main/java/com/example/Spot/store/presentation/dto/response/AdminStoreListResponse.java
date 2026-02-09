package com.example.Spot.store.presentation.dto.response;

import java.util.UUID;

import com.example.Spot.store.domain.StoreStatus;
import com.example.Spot.store.domain.entity.StoreEntity;

public record AdminStoreListResponse(
        UUID id,
        String name,
        String roadAddress,
        String addressDetail,
        String phoneNumber,
        StoreStatus status,
        boolean isDeleted


) {

    public static AdminStoreListResponse from(StoreEntity store) {
        return new AdminStoreListResponse(
                store.getId(),
                store.getName(),
                store.getRoadAddress(),
                store.getAddressDetail(),
                store.getPhoneNumber(),
                store.getStatus(),
                store.getIsDeleted()
        );
    }
}
