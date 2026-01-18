package com.example.Spot.store.presentation.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;

public record StoreDetailResponse(
        UUID id,
        String name,
        String roadAddress,
        String addressDetail,
        String phoneNumber,
        LocalTime openTime,
        LocalTime closeTime,
        List<String> categoryNames,
        StaffInfo owner,
        List<StaffInfo> chefs,
        List<MenuPublicResponseDto> menus,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record StaffInfo(Integer userId, String name, Role role) {}

    public static StoreDetailResponse fromEntity(StoreEntity store, List<MenuPublicResponseDto> menus) {
        // MSA: User 정보는 User 서비스에서 별도 조회 필요
        StaffInfo ownerInfo = null;
        List<StaffInfo> chefInfos = List.of();

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
                ownerInfo,
                chefInfos,
                menus,
                store.getIsDeleted(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}
