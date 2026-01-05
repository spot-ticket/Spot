package com.example.Spot.payments.presentation.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponseDto {

    @Builder
    public record Confirm(
        UUID paymentItemId,
        String status,
        Long amount,
        LocalDateTime approvedAt
    ) {}

    @Builder
    public record Cancel(
        UUID paymentId,
        Long cancelAmount,
        String cancelReason,
        LocalDateTime canceledAt
    ) {}

    @Builder
    public record View(

    ) {}

}
