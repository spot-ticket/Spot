package com.example.Spot.menu.presentation.dto.request;

import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMenuRequestDto {

    @NotNull(message = "가게 ID는 필수입니다.")
    @JsonProperty("store_id")
    private UUID storeId;

    @NotBlank(message = "메뉴명은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value =  0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    private String description;

    @NotBlank(message = "메뉴 이미지는 필수입니다.")
    @JsonProperty("image_url")
    private String imageUrl;

    public MenuEntity toEntity(StoreEntity store) {
        return MenuEntity.builder()
                .store(store)
                .name(name)
                .category(category)
                .price(price)
                .description(description)
                .imageUrl(imageUrl)
                .build();
    }
}
