package com.example.Spot.order.presentation.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDto {

    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<OrderItemRequestDto> orderItems;

    @NotNull(message = "픽업 시간은 필수입니다.")
    @Future(message = "픽업 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime pickupTime;

    @Builder.Default
    private Boolean needDisposables = false;

    @Size(max = 500, message = "요청사항은 500자 이내로 입력해주세요.")
    private String request;
}

