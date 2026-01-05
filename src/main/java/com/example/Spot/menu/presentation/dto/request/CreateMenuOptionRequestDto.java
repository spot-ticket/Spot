package com.example.Spot.menu.presentation.dto.request;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMenuOptionRequestDto {

    @NotBlank(message = "옵션명은 필수입니다.")
    private String name;

    private String detail;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value =  0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    public MenuOptionEntity toEntity(MenuEntity menu) {
        return MenuOptionEntity.builder()
                .menu(menu)
                .name(name)
                .detail(detail)
                .price(price)
                .build();
    }
}
