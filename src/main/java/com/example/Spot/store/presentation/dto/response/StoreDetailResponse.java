package com.example.Spot.store.presentation.dto.response;

import com.example.Spot.user.domain.Role;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.store.domain.entity.StoreEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public record StoreDetailResponse(
        UUID id,
        String name,
        String address,
        String detailAddress,
        String phoneNumber,
        LocalTime openTime,
        LocalTime closeTime,
        List<String> categoryNames,
        StaffInfo owner,
        List<StaffInfo> chefs,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record StaffInfo(Integer userId, String name) {}
    
    public static StoreDetailResponse fromEntity(StoreEntity store) {
        StaffInfo ownerInfo = store.getUsers().stream()
                .filter(su -> su.getUser().getRole() == Role.OWNER)
                .map(su -> new StaffInfo(su.getUser().getId(), su.getUser().getNickname()))
                .findFirst().orElse(null);

        List<StaffInfo> chefInfos = store.getUsers().stream()
                .filter(su -> su.getUser().getRole() == Role.CHEF)
                .map(su -> new StaffInfo(su.getUser().getId(), su.getUser().getNickname()))
                .toList();
        
        return new StoreDetailResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getDetailAddress(),
                store.getPhoneNumber(),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getStoreCategoryMaps().stream()
                        .map(map -> map.getCategory().getName())
                        .toList(),
                ownerInfo,
                chefInfos,
                store.getIsDeleted(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}
