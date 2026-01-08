package com.example.Spot.menu.presentation.dto.response;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public List<MenuOptionPublicResponseDto> options;

    public static MenuPublicResponseDto of(MenuEntity menu, List<MenuOptionEntity> options) {
        return MenuPublicResponseDto.builder()
                .id(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .category(menu.getCategory())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .imageUrl(menu.getImageUrl())
                .isAvailable(menu.getIsAvailable())
                // options가 null이면 빈 리스트 반환
                .options(options != null ?
                        options.stream()
                                .map(MenuOptionPublicResponseDto::new) // 생성자 사용
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }
}
