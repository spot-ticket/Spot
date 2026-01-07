package com.example.Spot.menu.presentation.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuPublicResponseDto implements MenuResponseDto {

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

    public static MenuPublicResponseDto of(MenuEntity menu, List<MenuOptionEntity> options) {
        MenuPublicResponseDto dto = new MenuPublicResponseDto();

        dto.id = menu.getId();
        dto.storeId = menu.getStore().getId();
        dto.name = menu.getName();
        dto.category = menu.getCategory();
        dto.price = menu.getPrice();
        dto.description = menu.getDescription();
        dto.imageUrl = menu.getImageUrl();
        dto.isAvailable = menu.getIsAvailable();

        // menu.getOptions() 대신 파라미터로 받은 options를 사용
        dto.options = options.stream()
                .map(MenuOptionResponseDto::new)
                .collect(Collectors.toList());

        return dto;
    }
}
