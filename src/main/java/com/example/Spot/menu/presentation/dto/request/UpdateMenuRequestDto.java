package com.example.Spot.menu.presentation.dto.request;

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
    private String imageUrl;
    private Boolean isAvailable;
}
