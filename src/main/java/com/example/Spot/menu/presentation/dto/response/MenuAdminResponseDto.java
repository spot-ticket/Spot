package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuAdminResponseDto implements MenuResponseDto {

    private UUID id;
    private UUID storeId;
    private String name;
    private String category;
    private Integer price;
    private String description;
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isDeleted;
    private Boolean isHidden;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<MenuOptionResponseDto> options;

    public MenuAdminResponseDto(MenuEntity menu, List<MenuOptionEntity> options) {
        this.id = menu.getId();
        this.storeId = menu.getStore().getId();
        this.name = menu.getName();
        this.category = menu.getCategory();
        this.price = menu.getPrice();
        this.description = menu.getDescription();
        this.imageUrl = menu.getImageUrl();
        this.isAvailable = menu.getIsAvailable();
        this.isDeleted = menu.getIsDeleted();
        this.isHidden = menu.getIsHidden();
        this.createdAt = menu.getCreatedAt();
        this.updatedAt = menu.getUpdatedAt();

        this.options = (options != null)
                ? options.stream().map(MenuOptionResponseDto::new).collect(Collectors.toList())
                : Collections.emptyList();
    }
}
