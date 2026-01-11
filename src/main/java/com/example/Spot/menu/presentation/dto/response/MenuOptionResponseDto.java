package com.example.Spot.menu.presentation.dto.response;

import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuOptionResponseDto {

    private UUID id;
    private UUID menuId;
    private String optionName;
    private String detail;
    private Integer optionPrice;
    private Boolean isAvailable;
    private Boolean isDeleted;

    public MenuOptionResponseDto(MenuOptionEntity menuOption) {
        this.id = menuOption.getId();
        this.menuId = menuOption.getMenu().getId();
        this.optionName = menuOption.getName();
        this.detail = menuOption.getDetail();
        this.optionPrice = menuOption.getPrice();
        this.isAvailable = menuOption.isAvailable();
        this.isDeleted = menuOption.getIsDeleted();
    }
}
