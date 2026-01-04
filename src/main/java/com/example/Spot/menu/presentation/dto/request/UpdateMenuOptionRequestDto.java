package com.example.Spot.menu.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMenuOptionRequestDto {
    private String name;
    private String detail;
    private Integer price;

    @JsonProperty("is_available")
    private Boolean isAvailable;
}
