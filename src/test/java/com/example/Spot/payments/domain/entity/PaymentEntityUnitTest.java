package com.example.Spot.payments.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class PaymentEntityUnitTest {

    private PaymentEntity createReadyPayment() {
        return PaymentEntity.builder()
                .title("테스트 결제")
                .paymentAmount(10000L)
                .idempotencyKey(UUID.randomUUID())
                .build();
    }
}
