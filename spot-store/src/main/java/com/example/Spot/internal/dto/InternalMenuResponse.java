package com.example.Spot.internal.dto;

import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalMenuResponse {

    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private Integer price;
    private String imageUrl;
    private boolean isHidden;
    private boolean isDeleted;

    public static InternalMenuResponse from(MenuEntity menu) {
        return InternalMenuResponse.builder()
                .id(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .isHidden(menu.isHidden())
                .isDeleted(menu.getIsDeleted())
                .build();
    }
}
