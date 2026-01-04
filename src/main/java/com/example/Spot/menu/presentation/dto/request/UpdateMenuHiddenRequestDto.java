package com.example.Spot.menu.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMenuHiddenRequestDto {

    @JsonProperty("is_hidden")
    private Boolean isHidden; // 딱 이것만 받습니다.
}
