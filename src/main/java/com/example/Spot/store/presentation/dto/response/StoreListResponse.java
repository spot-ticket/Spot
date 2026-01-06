package com.example.Spot.store.presentation.dto.response;

import java.util.List;
import java.util.UUID;

import com.example.Spot.store.domain.entity.StoreEntity;

public record StoreListResponse (
    
    UUID id,
    String name,
    String address,
    String phoneNumber,
    List<String> categoryNames
) {
    // Entity -> DTO 변환 메서드
    public static StoreListResponse fromEntity(StoreEntity store) {
        return new StoreListResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhoneNumber(),
                store.getStoreCategoryMaps().stream()
                        .map(map -> map.getCategory().getName())
                        .toList()
        );
    }
}
