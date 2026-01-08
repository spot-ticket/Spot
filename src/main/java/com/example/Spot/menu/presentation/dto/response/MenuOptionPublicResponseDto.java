package com.example.Spot.menu.presentation.dto.response;

import java.util.UUID;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuOptionPublicResponseDto { // 이름에 Public 명시

    @JsonProperty("option_id")
    private UUID id;

    @JsonProperty("menu_id")
    private UUID menuId;

    private String name;
    private String detail;
    private Integer price;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    public MenuOptionPublicResponseDto(MenuOptionEntity entity) {
        this.id = entity.getId();
        this.menuId = entity.getMenu().getId();
        this.name = entity.getName();
        this.detail = entity.getDetail();
        this.price = entity.getPrice();
        this.isAvailable = entity.getIsAvailable();
    }
}