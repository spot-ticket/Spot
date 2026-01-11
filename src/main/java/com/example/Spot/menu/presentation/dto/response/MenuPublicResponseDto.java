package com.example.Spot.menu.presentation.dto.response;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MenuPublicResponseDto(
        @JsonProperty("menu_id")
        UUID id,

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

        List<MenuOptionPublicResponseDto> options
) implements MenuResponseDto {

    // 정적 팩토리 메서드
    public static MenuPublicResponseDto of(MenuEntity menu, List<MenuOptionEntity> options) {
        // 옵션 변환 로직 (Null Safe)
        List<MenuOptionPublicResponseDto> optionDtos = (options != null)
                ? options.stream()
                .map(MenuOptionPublicResponseDto::from).toList()
                : Collections.emptyList();

        return new MenuPublicResponseDto(
                menu.getId(),
                menu.getStore().getId(),
                menu.getName(),
                menu.getCategory(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.getIsAvailable(),
                optionDtos
        );
    }
}
