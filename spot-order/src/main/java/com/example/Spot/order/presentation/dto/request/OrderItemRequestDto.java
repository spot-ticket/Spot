package com.example.Spot.order.presentation.dto.request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
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
public class OrderItemRequestDto {

    @NotNull(message = "메뉴 ID는 필수입니다.")
    private UUID menuId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;

    @Valid
    @Builder.Default
    private List<OrderItemOptionRequestDto> options = new ArrayList<>();
}

