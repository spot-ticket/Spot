package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateMenuResponseDto(
        @JsonProperty("menu_id")
        UUID menuId,

        @JsonProperty("store_id")
        UUID storeId,

        String name,

        String category,

        Integer price,

        String description,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("is_available")
        Boolean isAvailable,

        @JsonProperty("is_hidden")
        Boolean isHidden,

        @JsonProperty("updated_at")
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
