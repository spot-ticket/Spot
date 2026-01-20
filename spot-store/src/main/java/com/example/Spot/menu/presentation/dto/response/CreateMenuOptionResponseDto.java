package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

public record CreateMenuOptionResponseDto(

        UUID optionId,
        UUID menuId,
        String name,
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
