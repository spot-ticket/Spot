package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMenuOptionResponseDto {

    private UUID optionId;
    private UUID menuId;
    private String name;
    private LocalDateTime createdAt;

    public CreateMenuOptionResponseDto(MenuOptionEntity option) {
        this.optionId = option.getId();
        this.menuId = option.getMenu().getId();
        this.name = option.getName();
        this.createdAt = option.getCreatedAt();
    }
}
