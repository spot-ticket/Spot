package com.example.Spot.payments.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;

import lombok.Builder;

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
    public record PaymentDetail(
        UUID paymentId,
        String title,
        String content,
        PaymentMethod paymentMethod,
        Long totalAmount,
        String status,
        LocalDateTime createdAt
    ) {}

    @Builder
    public record PaymentList(
        List<PaymentDetail> payments,
        int totalCount
    ) {}

    @Builder
    public record CancelDetail(
        UUID cancelId,
        UUID paymentId,
        String cancelReason,
        Long cancelAmount,
        LocalDateTime canceledAt
    ) {}

    @Builder
    public record CancelList(
        List<CancelDetail> cancellations,
        int totalCount
    ) {}

}
