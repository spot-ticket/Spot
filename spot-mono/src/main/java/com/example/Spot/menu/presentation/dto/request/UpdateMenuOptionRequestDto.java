package com.example.Spot.menu.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateMenuOptionRequestDto(
        @Size(min = 1, max = 50, message = "옵션명은 1자 이상 50자 이하여야 합니다.")
        String name,

        @Size(max = 50, message = "상세 설명은 50자를 초과할 수 없습니다.")
        String detail,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @JsonProperty("is_available")
        Boolean isAvailable
) {
}
