package com.example.Spot.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRejectRequestDto {

    @NotBlank(message = "거절 사유는 필수입니다.")
    @Size(max = 500, message = "거절 사유는 500자 이내로 입력해주세요.")
    private String reason;
}

