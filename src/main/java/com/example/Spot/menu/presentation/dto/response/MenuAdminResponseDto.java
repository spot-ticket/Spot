package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuAdminResponseDto implements MenuResponseDto {

    @JsonProperty("menu_id")
    private UUID id;

    @JsonProperty("store_id")
    private UUID storeId;

    private String name;
    private String category;
    private Integer price;
    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("is_hidden")
    private Boolean isHidden;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("options")
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
