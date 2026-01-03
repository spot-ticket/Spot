package com.example.Spot.payments.presentation.dto.request;

import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

public class PaymentRequestDto {

    @Builder
    public record Confirm(
        @NotNull String title,
        @NotNull String content,
        @NotNull Integer userId,
        @NotNull UUID orderId,
        @NotNull PaymentMethod paymentMethod,
        @NotNull Long paymentAmount
    ) {}

    @Builder
    public record Cancel(
        @NotNull UUID paymentId,
        @NotNull String cancelReason
    ) {}


}
