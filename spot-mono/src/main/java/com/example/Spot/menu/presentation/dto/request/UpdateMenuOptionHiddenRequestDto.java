package com.example.Spot.menu.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record UpdateMenuOptionHiddenRequestDto(
        @NotNull(message = "숨김 여부는 필수입니다.")
        @JsonProperty("is_hidden")
        Boolean isHidden
) {
}
