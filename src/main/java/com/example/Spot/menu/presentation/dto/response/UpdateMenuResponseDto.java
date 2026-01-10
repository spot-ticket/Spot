package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateMenuResponseDto(

        UUID menuId,
        UUID storeId,
        String name,
        String category,
        Integer price,
        String description,
        String imageUrl,
        Boolean isAvailable,
        Boolean isHidden,
        LocalDateTime updatedAt
) {
    // Entity -> DTO 변환 생성자
    public UpdateMenuResponseDto(MenuEntity menu) {
        this(
                menu.getId(),
                menu.getStore().getId(),
                menu.getName(),
                menu.getCategory(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.getIsAvailable(),
                menu.getIsHidden(),
                menu.getUpdatedAt()
        );
    }
}
