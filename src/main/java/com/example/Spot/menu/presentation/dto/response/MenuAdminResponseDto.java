package com.example.Spot.menu.presentation.dto.response;

import com.example.Spot.menu.domain.entity.MenuEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class MenuResponseDto {
    private UUID id;
    private String name;
    private String category;
    private Integer price;
    private Boolean isHidden;

    public MenuResponseDto(MenuEntity menuEntity) {
        this.id = menuEntity.getId();
        this.name = menuEntity.getName();
        this.category = menuEntity.getCategory();
        this.price = menuEntity.getPrice();
        this.isHidden = menuEntity.getIsHidden();
    }
}
