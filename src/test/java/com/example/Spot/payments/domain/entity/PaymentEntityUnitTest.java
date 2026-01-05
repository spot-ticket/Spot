package com.example.Spot.payments.domain.entity;

import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;

public class PaymentEntityUnitTest {

    private PaymentEntity createReadyPayment() {
        return PaymentEntity.builder()
                .title("테스트 결제")
                .content("결제 테스트 진행입니다")
                .totalAmount(10000L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
    }
}
