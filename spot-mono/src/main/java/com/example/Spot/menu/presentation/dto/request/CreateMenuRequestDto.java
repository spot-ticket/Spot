package com.example.Spot.menu.presentation.dto.request;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMenuRequestDto(
        @NotBlank(message = "메뉴명은 필수입니다.")
        String name,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        String description,

        @JsonProperty("image_url")
        String imageUrl
) {
    // Record 내부에 편의 메서드 작성 가능
    public MenuEntity toEntity(StoreEntity store) {
        return MenuEntity.builder()
                .store(store)
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .description(this.description)
                .imageUrl(this.imageUrl)
                .build();
    }
}
