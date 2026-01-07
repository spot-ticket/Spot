package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
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

    public MenuAdminResponseDto(MenuEntity menu) {
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
    }
}
