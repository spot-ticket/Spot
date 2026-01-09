package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateMenuResponseDto(
        @JsonProperty("menu_id")
        UUID id,

        String name,

        String category,

        Integer price,

        String description,

        @JsonProperty("image_url")
        String imageUrl,

        LocalDateTime createdAt
) {
    // Entity를 DTO로 변환하는 생성자
    public CreateMenuResponseDto(MenuEntity menu) {
        this(
                menu.getId(),
                menu.getName(),
                menu.getCategory(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.getCreatedAt()
        );
    }
}
