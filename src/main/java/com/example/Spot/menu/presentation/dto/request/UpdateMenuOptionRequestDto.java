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
@AllArgsConstructor // Builder 사용을 위해 필요
@Builder            // 일관성 있는 객체 생성을 위해 추가
public class UpdateMenuOptionRequestDto {

    // 부분 수정을 위해 필수 조건(@NotBlank) 제거, 길이 제한(@Size)만 유지
    @Size(min = 1, max = 50, message = "옵션명은 1자 이상 50자 이하여야 합니다.")
    private String name;

    @Size(max = 50, message = "상세 설명은 50자를 초과할 수 없습니다.")
    private String detail;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @JsonProperty("is_available")
    private Boolean isAvailable;
}