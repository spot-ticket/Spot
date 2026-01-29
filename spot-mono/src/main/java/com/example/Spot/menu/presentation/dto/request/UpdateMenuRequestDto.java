package com.example.Spot.menu.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateMenuRequestDto(

        @Size(min = 1, max = 50, message = "메뉴명은 1자 이상 50자 이하여야 합니다.")
        String name,

        @Size(max = 20, message = "카테고리는 20자를 초과할 수 없습니다.")
        String category,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @Size(max = 100, message = "설명은 100자를 초과할 수 없습니다.")
        String description,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("is_available")
        Boolean isAvailable
) {
}
