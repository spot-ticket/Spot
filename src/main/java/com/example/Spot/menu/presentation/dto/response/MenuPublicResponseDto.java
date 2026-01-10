package com.example.Spot.menu.presentation.dto.response;

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
public class MenuPublicResponseDto implements MenuResponseDto {

    private UUID id;
    private UUID storeId;
    private String name;
    private String category;
    private Integer price;
    private String description;
    private String imageUrl;
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

        // [수정 포인트] options가 null이면 에러 대신 빈 리스트([]) 반환
        dto.options = (options != null)
                ? options.stream().map(MenuOptionResponseDto::new).collect(Collectors.toList())
                : Collections.emptyList();

        return dto;
    }
}
