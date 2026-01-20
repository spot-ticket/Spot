package com.example.Spot.payments.domain.gateway;

import java.util.UUID;

import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;

public interface PaymentGateway {

    TossPaymentResponse requestBillingPayment(
        String billingKey,
        Long amount,
        UUID orderId,
        String orderName,
        String customerKey,
        Integer timeout
    );

    TossPaymentResponse cancelPayment(
        String paymentKey,
        String cancelReason,
        Integer timeout
    );

    TossPaymentResponse cancelPaymentPartial(
        String paymentKey,
        Long cancelAmount,
        String cancelReason
    );

    TossPaymentResponse issueBillingKey(
        String authKey,
        String customerKey
    );
}
