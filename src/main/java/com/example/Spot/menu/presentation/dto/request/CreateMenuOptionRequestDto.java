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
    private String optionName;
    private String optionDetail;

    @NotNull(message = "메뉴 옵션 가격은 필수입니다.")
    @Min(value =  0, message = "메뉴 옵션 추가 가격은 0원 이상이어야 합니다.")
    private Integer optionPrice;

    public MenuOptionEntity toEntity(MenuEntity menu) {
        return MenuOptionEntity.builder()
                .menu(menu)
                .name(optionName)
                .detail(optionDetail)
                .price(optionPrice)
                .build();
    }
}
