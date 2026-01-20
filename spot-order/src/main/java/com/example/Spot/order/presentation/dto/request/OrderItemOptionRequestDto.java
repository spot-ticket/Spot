package com.example.Spot.order.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionRequestDto {

    @NotNull(message = "옵션 ID는 필수입니다.")
    private UUID menuOptionId;
}

