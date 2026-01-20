package com.example.Spot.internal.dto;

import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalMenuOptionResponse {

    private UUID id;
    private UUID menuId;
    private String name;
    private String detail;
    private Integer price;
    private boolean isDeleted;

    public static InternalMenuOptionResponse from(MenuOptionEntity menuOption) {
        return InternalMenuOptionResponse.builder()
                .id(menuOption.getId())
                .menuId(menuOption.getMenu().getId())
                .name(menuOption.getName())
                .detail(menuOption.getDetail())
                .price(menuOption.getPrice())
                .isDeleted(menuOption.getIsDeleted())
                .build();
    }
}
