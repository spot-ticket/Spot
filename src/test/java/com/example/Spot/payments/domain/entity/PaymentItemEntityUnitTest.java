package com.example.Spot.payments.domain.entity;

import com.example.Spot.order.domain.entity.OrderEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;

class PaymentItemEntityTest {

    @Test
    @DisplayName("정상: 결제와 주문 정보가 모두 있으면 생성에 성공한다")
    void 결제_Item_정상_생성_테스트() {

        PaymentEntity mockPayment = Mockito.mock(PaymentEntity.class);
        OrderEntity mockOrder = Mockito.mock(OrderEntity.class);

        PaymentItemEntity paymentItem = PaymentItemEntity.builder()
                .payment(mockPayment)
                .order(mockOrder)
                .build();

        assertThat(paymentItem.getPayment()).isEqualTo(mockPayment);
        assertThat(paymentItem.getOrder()).isEqualTo(mockOrder);
    }

    @Test
    @DisplayName("예외: 결제 정보가 null이면 IllegalArgumentException이 발생한다")
    void 결제_정보_부재_예외_테스트() {

        OrderEntity mockOrder = Mockito.mock(OrderEntity.class);

        assertThatThrownBy(() -> PaymentItemEntity.builder()
                .payment(null)
                .order(mockOrder)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사전에 등록된 결제가 있어야 합니다.");
    }

    @Test
    @DisplayName("예외: 주문 정보가 null이면 IllegalArgumentException이 발생한다")
    void 결제_주문_정보_부재_예외_테스트() {

        PaymentEntity mockPayment = Mockito.mock(PaymentEntity.class);

        assertThatThrownBy(() -> PaymentItemEntity.builder()
                .payment(mockPayment)
                .order(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사전에 등록된 주문이 있어야 합니다.");
    }
}