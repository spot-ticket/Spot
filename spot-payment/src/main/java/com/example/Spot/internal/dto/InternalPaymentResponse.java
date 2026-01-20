package com.example.Spot.internal.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalPaymentResponse {

    private UUID id;
    private UUID orderId;
    private Integer userId;
    private BigDecimal amount;
    private String status;
    private String paymentKey;

    public static InternalPaymentResponse from(PaymentEntity payment, PaymentKeyEntity paymentKey) {
        return InternalPaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(BigDecimal.valueOf(payment.getTotalAmount()))
                .status("ACTIVE")
                .paymentKey(paymentKey != null ? paymentKey.getPaymentKey() : null)
                .build();
    }
}
