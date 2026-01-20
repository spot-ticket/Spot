package com.example.Spot.store.presentation.dto.response;

import java.util.List;
import java.util.UUID;

import com.example.Spot.store.domain.StoreStatus;
import com.example.Spot.store.domain.entity.StoreEntity;

public record StoreListResponse (

    UUID id,
    String name,
    String roadAddress,
    String addressDetail,
    String phoneNumber,
    List<String> categoryNames,
    StoreStatus status,
    boolean isDeleted
) {
    // Entity -> DTO 변환 메서드
    public static StoreListResponse fromEntity(StoreEntity store) {
        return new StoreListResponse(
                store.getId(),
                store.getName(),
                store.getRoadAddress(),
                store.getAddressDetail(),
                store.getPhoneNumber(),
                store.getStoreCategoryMaps().stream()
                        .map(map -> map.getCategory().getName())
                        .toList(),
                store.getStatus(),
                store.getIsDeleted()
        );
    }
}
