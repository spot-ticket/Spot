package com.example.Spot.payments.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;

class PaymentEntityUnitTest {

  @Nested
  @DisplayName("PaymentEntity 생성 테스트")
  class CreatePaymentEntityTest {

    @Test
    @DisplayName("정상: 모든 필드가 유효하면 PaymentEntity 생성에 성공한다")
    void 결제_엔티티_정상_생성_테스트() {

      Integer userId = 1;
      UUID orderId = UUID.randomUUID();
      String title = "치킨 주문 결제";
      String content = "후라이드 치킨 1마리";
      PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
      Long totalAmount = 18000L;

      PaymentEntity payment =
          PaymentEntity.builder()
              .userId(userId)
              .orderId(orderId)
              .title(title)
              .content(content)
              .paymentMethod(paymentMethod)
              .totalAmount(totalAmount)
              .build();

      assertThat(payment.getUserId()).isEqualTo(userId);
      assertThat(payment.getOrderId()).isEqualTo(orderId);
      assertThat(payment.getPaymentTitle()).isEqualTo(title);
      assertThat(payment.getPaymentContent()).isEqualTo(content);
      assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
      assertThat(payment.getTotalAmount()).isEqualTo(totalAmount);
    }

    @Test
    @DisplayName("정상: 계좌이체 결제 방식으로 생성할 수 있다")
    void 계좌이체_결제_생성_테스트() {

      Integer userId = 1;
      UUID orderId = UUID.randomUUID();

      PaymentEntity payment =
          PaymentEntity.builder()
              .userId(userId)
              .orderId(orderId)
              .title("테스트 결제")
              .content("테스트 내용")
              .paymentMethod(PaymentMethod.BANK_TRANSFER)
              .totalAmount(50000L)
              .build();

      assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
    }
  }

  @Nested
  @DisplayName("PaymentEntity 생성 실패 테스트")
  class CreatePaymentEntityFailTest {

    @Test
    @DisplayName("예외: userId가 null이면 IllegalArgumentException이 발생한다")
    void userId_null_예외_테스트() {

      UUID orderId = UUID.randomUUID();

      assertThatThrownBy(
              () ->
                  PaymentEntity.builder()
                      .userId(null)
                      .orderId(orderId)
                      .title("테스트 결제")
                      .content("테스트 내용")
                      .paymentMethod(PaymentMethod.CREDIT_CARD)
                      .totalAmount(10000L)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("PaymentEntity에 Order ID를 입력하지 않았습니다.");
    }

    @Test
    @DisplayName("예외: orderId가 null이면 IllegalArgumentException이 발생한다")
    void orderId_null_예외_테스트() {

      Integer userId = 1;

      assertThatThrownBy(
              () ->
                  PaymentEntity.builder()
                      .userId(userId)
                      .orderId(null)
                      .title("테스트 결제")
                      .content("테스트 내용")
                      .paymentMethod(PaymentMethod.CREDIT_CARD)
                      .totalAmount(10000L)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("PaymentEntity에 User ID를 입력하지 않았습니다.");
    }
  }

  @Nested
  @DisplayName("PaymentMethod Enum 테스트")
  class PaymentMethodEnumTest {

    @Test
    @DisplayName("정상: PaymentMethod enum 값들이 올바르게 정의되어 있다")
    void 결제_방식_enum_테스트() {

      assertThat(PaymentMethod.values()).hasSize(2);
      assertThat(PaymentMethod.valueOf("CREDIT_CARD")).isEqualTo(PaymentMethod.CREDIT_CARD);
      assertThat(PaymentMethod.valueOf("BANK_TRANSFER")).isEqualTo(PaymentMethod.BANK_TRANSFER);
    }
  }
}
