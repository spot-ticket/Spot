package com.example.Spot.menu.presentation.dto.response;

import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuOptionPublicResponseDto(

        UUID id,
        UUID menuId,
        String name,
        String detail,
        Integer price,
        Boolean isAvailable
) {
    public static MenuOptionPublicResponseDto from(MenuOptionEntity entity) {
        return new MenuOptionPublicResponseDto(
                entity.getId(),
                entity.getMenu().getId(),
                entity.getName(),
                entity.getDetail(),
                entity.getPrice(),
                entity.isAvailable()
        );
    }
}
