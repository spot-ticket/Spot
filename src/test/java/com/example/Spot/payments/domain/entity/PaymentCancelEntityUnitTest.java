package com.example.Spot.payments.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.Spot.order.domain.entity.OrderEntity;

public class PaymentCancelEntityUnitTest {

    @Test
    @DisplayName("정상: 결제 취소 엔티티가 모든 정보를 포함하여 생성되어야 한다")
    void 결제_취소_정보_부재_테스트() {
        UUID idempotency = UUID.randomUUID();
        String reason = "고객 변심";

        OrderEntity order = OrderEntity.builder()
                .orderNumber("TEST-ORDER-001")
                .userId(1L)
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        PaymentEntity payment = PaymentEntity.builder()
                .title("테스트 결제")
                .content("테스트 결제 내용")
                .idempotencyKey(UUID.randomUUID())
                .paymentMethod(PaymentEntity.PaymentMethod.CREDIT_CARD)
                .paymentAmount(10000L)
                .paymentStatus(PaymentEntity.PaymentStatus.SUCCESS)
                .build();

        PaymentItemEntity paymentItem = PaymentItemEntity.builder()
                .payment(payment)
                .order(order)
                .build();

        PaymentCancelEntity cancel = PaymentCancelEntity.builder()
                .paymentItem(paymentItem)
                .reason(reason)
                .cancelIdempotency(idempotency)
                .build();

        assertThat(cancel.getReason()).isEqualTo(reason);
        assertThat(cancel.getCancelIdempotency()).isEqualTo(idempotency);
        assertThat(cancel.getPaymentItem()).isEqualTo(paymentItem);
    }

    @Test
    @DisplayName("예외: 취소 사유가 없으면 엔티티 생성에 실패한다")
    void reasonNullTest() {
        assertThatThrownBy(() -> PaymentCancelEntity.builder()
                .reason(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
