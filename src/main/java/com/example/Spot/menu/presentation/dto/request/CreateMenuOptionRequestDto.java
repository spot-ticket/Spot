package com.example.Spot.menu.presentation.dto.request;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMenuOptionRequestDto(
        @NotBlank(message = "옵션명은 필수입니다.")
        String name,

        String detail,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price
) {
    public MenuOptionEntity toEntity(MenuEntity menu) {
        return MenuOptionEntity.builder()
                .menu(menu)
                .name(this.name)
                .detail(this.detail)
                .price(this.price)
                .build();
    }
}
