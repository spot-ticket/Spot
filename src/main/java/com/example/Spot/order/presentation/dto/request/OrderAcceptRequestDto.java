package com.example.Spot.order.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAcceptRequestDto {

    @NotNull(message = "예상 조리 시간은 필수입니다.")
    @Min(value = 1, message = "예상 조리 시간은 1분 이상이어야 합니다.")
    private Integer estimatedTime;
}

