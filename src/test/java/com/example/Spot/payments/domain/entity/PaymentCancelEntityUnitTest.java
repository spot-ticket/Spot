package com.example.Spot.payments.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentCancelEntityUnitTest {

  @Nested
  @DisplayName("PaymentCancelEntity 생성 테스트")
  class PaymentCancelEntityCreateTest {

    @Test
    @DisplayName("정상: 결제 취소 엔티티가 모든 정보를 포함하여 생성되어야 한다")
    void 결제_취소_엔티티_정상_생성_테스트() {
      UUID paymentHistoryId = UUID.randomUUID();
      String reason = "고객 변심";

      PaymentCancelEntity cancel =
          PaymentCancelEntity.builder().paymentHistoryId(paymentHistoryId).reason(reason).build();

      assertThat(cancel.getPaymentHistoryId()).isEqualTo(paymentHistoryId);
      assertThat(cancel.getReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("정상: 다양한 취소 사유로 생성할 수 있다")
    void 다양한_취소_사유_생성_테스트() {
      UUID paymentHistoryId = UUID.randomUUID();
      String reason = "상품 품절로 인한 취소";

      PaymentCancelEntity cancel =
          PaymentCancelEntity.builder().paymentHistoryId(paymentHistoryId).reason(reason).build();

      assertThat(cancel.getReason()).isEqualTo("상품 품절로 인한 취소");
    }
  }

  @Nested
  @DisplayName("PaymentCancelEntity 생성 실패 테스트")
  class PaymentCancelEntityCreateFailTest {

    @Test
    @DisplayName("예외: 취소 사유가 null이면 IllegalArgumentException이 발생한다")
    void 취소_사유_null_예외_테스트() {
      UUID paymentHistoryId = UUID.randomUUID();

      assertThatThrownBy(
              () ->
                  PaymentCancelEntity.builder()
                      .paymentHistoryId(paymentHistoryId)
                      .reason(null)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("취소 사유는 필수입니다.");
    }

    @Test
    @DisplayName("예외: 취소 사유가 빈 문자열이면 IllegalArgumentException이 발생한다")
    void 취소_사유_빈문자열_예외_테스트() {
      UUID paymentHistoryId = UUID.randomUUID();

      assertThatThrownBy(
              () ->
                  PaymentCancelEntity.builder()
                      .paymentHistoryId(paymentHistoryId)
                      .reason("")
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("취소 사유는 필수입니다.");
    }

    @Test
    @DisplayName("예외: 취소 사유가 공백 문자열이면 IllegalArgumentException이 발생한다")
    void 취소_사유_공백문자열_예외_테스트() {
      UUID paymentHistoryId = UUID.randomUUID();

      assertThatThrownBy(
              () ->
                  PaymentCancelEntity.builder()
                      .paymentHistoryId(paymentHistoryId)
                      .reason("   ")
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("취소 사유는 필수입니다.");
    }

    @Test
    @DisplayName("예외: paymentHistoryId가 null이면 IllegalArgumentException이 발생한다")
    void 결제_이력_아이디_null_예외_테스트() {
      assertThatThrownBy(
              () -> PaymentCancelEntity.builder().paymentHistoryId(null).reason("고객 변심").build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("취소 대상 아이템은 필수입니다.");
    }

    @Test
    @DisplayName("예외: 모든 필드가 null이면 IllegalArgumentException이 발생한다")
    void 모든_필드_null_예외_테스트() {
      assertThatThrownBy(
              () -> PaymentCancelEntity.builder().paymentHistoryId(null).reason(null).build())
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
