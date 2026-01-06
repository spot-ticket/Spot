package com.example.Spot.menu.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor // Builder 사용을 위해 필요
@Builder            // 테스트 및 코드 작성 시 편리함 제공
public class UpdateMenuRequestDto {
    private String name;
    private String category;
    private Integer price;
    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_available")
    private Boolean isAvailable;
}
