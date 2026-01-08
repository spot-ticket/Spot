package com.example.Spot.menu.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMenuRequestDto {

    // PATCH는 변경할 값만 보내므로 @NotBlank, @NotNull 제거

    @Size(min = 1, max = 50, message = "메뉴명은 1자 이상 50자 이하여야 합니다.")
    private String name;

    @Size(max = 20, message = "카테고리는 20자를 초과할 수 없습니다.")
    private String category;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @Size(max = 100, message = "설명은 100자를 초과할 수 없습니다.")
    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("is_hidden")
    private Boolean isHidden;
}