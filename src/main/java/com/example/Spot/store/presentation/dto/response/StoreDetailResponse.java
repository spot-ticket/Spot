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
        StaffInfo ownerInfo = store.getUsers().stream()
                .filter(su -> su.getUser().getRole() == Role.OWNER)
                .map(su -> new StaffInfo(
                        su.getUser().getId(),
                        su.getUser().getNickname(),
                        su.getUser().getRole()
                ))
                .findFirst().orElse(null);

        List<StaffInfo> chefInfos = store.getUsers().stream()
                .filter(su -> su.getUser().getRole() == Role.CHEF)
                .map(su -> new StaffInfo(
                        su.getUser().getId(),
                        su.getUser().getNickname(),
                        su.getUser().getRole()
                ))
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
                ownerInfo,
                chefInfos,
                menus,
                store.getIsDeleted(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}
