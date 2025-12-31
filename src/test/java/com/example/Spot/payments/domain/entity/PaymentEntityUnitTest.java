package com.example.Spot.payments.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentStatus;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

public class PaymentEntityUnitTest {

    private PaymentEntity createReadyPayment() {
        return PaymentEntity.builder()
                .title("테스트 결제")
                .paymentAmount(10000L)
                .idempotencyKey(UUID.randomUUID())
                .paymentStatus(PaymentStatus.READY)
                .build();
    }

    @Test
    @DisplayName("정상 : 상태 변경이 정상적으로 수행되어야 한다")
    void updateStatusTest() {
        PaymentEntity toSuccess = createReadyPayment();
        toSuccess.updateStatus(PaymentStatus.SUCCESS);
        assertThat(toSuccess.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);

        PaymentEntity toFailed = createReadyPayment();
        toFailed.updateStatus(PaymentStatus.FAILED);
        assertThat(toFailed.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        
        PaymentEntity toCancelled = createReadyPayment();
        toCancelled.updateStatus(PaymentStatus.CANCELLED);
        assertThat(toCancelled.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"READY", "FAILED", "CANCELLED"})
    @DisplayName("예외 : 이미 성공한 결제는 상태를 변경할 수 없다")
    void successStatusTransitionTest(PaymentStatus targetStatus) {

        PaymentEntity payment = PaymentEntity.builder()
                .paymentStatus(PaymentEntity.PaymentStatus.SUCCESS)
                .build();

        assertThatThrownBy(() -> payment.updateStatus(targetStatus))
            .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"READY", "FAILED", "CANCELLED"})
    @DisplayName("예외 : 이미 실패한 결제는 다른 상태로 변경할 수 없다")
    void failStatusTransitionTest(PaymentStatus targetStatus) {

        PaymentEntity payment = PaymentEntity.builder()
                .paymentStatus(PaymentEntity.PaymentStatus.FAILED)
                .build();
        
        assertThatThrownBy(() -> payment.updateStatus(targetStatus))
            .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"READY", "FAILED", "CANCELLED"})
    @DisplayName("예외 : 이미 취소한 결제는 다른 상태로 변경할 수 없다")
    void cancelStatusTransitionTest(PaymentStatus targetStatus) {

        PaymentEntity payment = PaymentEntity.builder()
                .paymentStatus(PaymentEntity.PaymentStatus.CANCELLED)
                .build();
        
        assertThatThrownBy(() -> payment.updateStatus(targetStatus))
            .isInstanceOf(IllegalStateException.class);
    }
}