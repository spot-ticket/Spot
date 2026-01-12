package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateMenuOptionResponseDto(
        @JsonProperty("option_id")
        UUID optionId,

        @JsonProperty("menu_id")
        UUID menuId,

        String name,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {

    public static CreateMenuOptionResponseDto from(MenuOptionEntity option) {
        return new CreateMenuOptionResponseDto(
                option.getId(),
                option.getMenu().getId(),
                option.getName(),
                option.getCreatedAt()
        );
    }
}
