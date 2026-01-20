package com.example.spotstore.store.presentation.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;

public record StoreDetailResponse(
        UUID id,
        String name,
        String roadAddress,
        String addressDetail,
        String phoneNumber,
        LocalTime openTime,
        LocalTime closeTime,
        List<String> categoryNames,
        List<Integer> staffUserIds,
        List<MenuPublicResponseDto> menus,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StoreDetailResponse fromEntity(StoreEntity store, List<MenuPublicResponseDto> menus) {
        // MSA 전환으로 userId만 반환 (User 정보는 별도 서비스에서 조회)
        List<Integer> staffUserIds = store.getUsers().stream()
                .map(su -> su.getUserId())
                .toList();

        return new StoreDetailResponse(
                store.getId(),
                store.getName(),
                store.getRoadAddress(),
                store.getAddressDetail(),
                store.getPhoneNumber(),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getStoreCategoryMaps().stream()
                        .map(map -> map.getCategory().getName())
                        .toList(),
                staffUserIds,
                menus,
                store.getIsDeleted(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}
