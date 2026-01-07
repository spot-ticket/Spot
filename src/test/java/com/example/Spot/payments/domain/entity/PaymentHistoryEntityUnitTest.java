package com.example.Spot.payments.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.Spot.payments.domain.entity.PaymentHistoryEntity.PaymentStatus;

class PaymentHistoryEntityUnitTest {

  @Nested
  @DisplayName("PaymentHistoryEntity 생성 테스트")
  class CreatePaymentHistoryEntityTest {

    @Test
    @DisplayName("정상: paymentId와 status가 유효하면 PaymentHistoryEntity 생성에 성공한다")
    void 결제_이력_정상_생성_테스트() {

      UUID paymentId = UUID.randomUUID();
      PaymentStatus status = PaymentStatus.READY;

      PaymentHistoryEntity history =
          PaymentHistoryEntity.builder().paymentId(paymentId).status(status).build();

      assertThat(history.getPaymentId()).isEqualTo(paymentId);
      assertThat(history.getStatus()).isEqualTo(PaymentStatus.READY);
      assertThat(history.getPaymentStatus()).isEqualTo(PaymentStatus.READY);
    }

    @Test
    @DisplayName("정상: IN_PROGRESS 상태로 생성할 수 있다")
    void 결제_진행중_상태_생성_테스트() {

      UUID paymentId = UUID.randomUUID();

      PaymentHistoryEntity history =
          PaymentHistoryEntity.builder()
              .paymentId(paymentId)
              .status(PaymentStatus.IN_PROGRESS)
              .build();

      assertThat(history.getStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("정상: DONE 상태로 생성할 수 있다")
    void 결제_완료_상태_생성_테스트() {

      UUID paymentId = UUID.randomUUID();

      PaymentHistoryEntity history =
          PaymentHistoryEntity.builder().paymentId(paymentId).status(PaymentStatus.DONE).build();

      assertThat(history.getStatus()).isEqualTo(PaymentStatus.DONE);
    }

    @Test
    @DisplayName("정상: CANCELLED 상태로 생성할 수 있다")
    void 결제_취소_상태_생성_테스트() {

      UUID paymentId = UUID.randomUUID();

      PaymentHistoryEntity history =
          PaymentHistoryEntity.builder()
              .paymentId(paymentId)
              .status(PaymentStatus.CANCELLED)
              .build();

      assertThat(history.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("정상: ABORTED 상태로 생성할 수 있다")
    void 결제_실패_상태_생성_테스트() {

      UUID paymentId = UUID.randomUUID();

      PaymentHistoryEntity history =
          PaymentHistoryEntity.builder().paymentId(paymentId).status(PaymentStatus.ABORTED).build();

      assertThat(history.getStatus()).isEqualTo(PaymentStatus.ABORTED);
    }
  }

  @Nested
  @DisplayName("PaymentHistoryEntity 생성 실패 테스트")
  class CreatePaymentHistoryEntityFailTest {

    @Test
    @DisplayName("예외: paymentId가 null이면 IllegalArgumentException이 발생한다")
    void paymentId_null_예외_테스트() {

      assertThatThrownBy(
              () ->
                  PaymentHistoryEntity.builder()
                      .paymentId(null)
                      .status(PaymentStatus.READY)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("사전에 등록된 결제가 있어야 합니다.");
    }
  }

  @Nested
  @DisplayName("PaymentStatus Enum 테스트")
  class PaymentStatusEnumTest {

    @Test
    @DisplayName("정상: PaymentStatus enum 값들이 올바르게 정의되어 있다")
    void 결제_상태_enum_테스트() {

      assertThat(PaymentStatus.values()).hasSize(9);
      assertThat(PaymentStatus.valueOf("READY")).isEqualTo(PaymentStatus.READY);
      assertThat(PaymentStatus.valueOf("IN_PROGRESS")).isEqualTo(PaymentStatus.IN_PROGRESS);
      assertThat(PaymentStatus.valueOf("WAITING_FOR_DEPOSIT"))
          .isEqualTo(PaymentStatus.WAITING_FOR_DEPOSIT);
      assertThat(PaymentStatus.valueOf("DONE")).isEqualTo(PaymentStatus.DONE);
      assertThat(PaymentStatus.valueOf("CANCELLED")).isEqualTo(PaymentStatus.CANCELLED);
      assertThat(PaymentStatus.valueOf("CANCELLED_IN_PROGRESS"))
          .isEqualTo(PaymentStatus.CANCELLED_IN_PROGRESS);
      assertThat(PaymentStatus.valueOf("PARTIAL_CANCELD")).isEqualTo(PaymentStatus.PARTIAL_CANCELD);
      assertThat(PaymentStatus.valueOf("ABORTED")).isEqualTo(PaymentStatus.ABORTED);
      assertThat(PaymentStatus.valueOf("EXPIRED")).isEqualTo(PaymentStatus.EXPIRED);
    }

    @Test
    @DisplayName("정상: 결제 흐름 상태들이 올바르게 정의되어 있다 (READY -> IN_PROGRESS -> DONE/ABORTED)")
    void 결제_흐름_상태_테스트() {
      // 정상 결제 흐름: READY -> IN_PROGRESS -> DONE
      assertThat(PaymentStatus.READY).isNotNull();
      assertThat(PaymentStatus.IN_PROGRESS).isNotNull();
      assertThat(PaymentStatus.DONE).isNotNull();

      // 실패 흐름: READY -> IN_PROGRESS -> ABORTED
      assertThat(PaymentStatus.ABORTED).isNotNull();

      // 취소 흐름: DONE -> CANCELLED_IN_PROGRESS -> CANCELLED
      assertThat(PaymentStatus.CANCELLED_IN_PROGRESS).isNotNull();
      assertThat(PaymentStatus.CANCELLED).isNotNull();
    }
  }
}
