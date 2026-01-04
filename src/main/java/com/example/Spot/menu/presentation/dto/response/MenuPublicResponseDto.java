package com.example.Spot.menu.presentation.dto.response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.menu.domain.entity.MenuEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class MenuPublicResponseDto {

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

    private List<MenuOptionResponseDto> options;

    public MenuPublicResponseDto(MenuEntity menu) {
        this.id = menu.getId();
        this.storeId = menu.getStore().getId();
        this.name = menu.getName();
        this.category = menu.getCategory();
        this.price = menu.getPrice();
        this.description = menu.getDescription();
        this.imageUrl = menu.getImageUrl();
        this.isAvailable = menu.getIsAvailable();

        this.options = menu.getOptions().stream()
                .filter(opt -> !opt.getIsDeleted())
                .filter(opt -> Boolean.TRUE.equals(opt.getIsAvailable()))
                .map(MenuOptionResponseDto::new)
                .collect(Collectors.toList());
    }
}
