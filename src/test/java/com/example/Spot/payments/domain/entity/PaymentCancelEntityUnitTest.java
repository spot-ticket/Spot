package com.example.Spot.payments.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

public class PaymentCancelEntityUnitTest {

    @Test
    @DisplayName("정상: 결제 취소 엔티티가 모든 정보를 포함하여 생성되어야 한다")
    void 결제_취소_정보_부재_테스트() {
        UUID idempotency = UUID.randomUUID();
        String reason = "고객 변심";

        UserEntity user = createTestUser();
        StoreEntity store = createTestStore();
        OrderEntity order = createTestOrder(store);
        PaymentEntity payment = createTestPayment(user);

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

    private UserEntity createTestUser() {
        return UserEntity.builder()
                         .username("김구름")
                         .nickname("구르미")
                         .roadAddress("서울시 강남구")
                         .addressDetail("123-45")
                         .email("goormi@goorm.com")
                         .role(Role.OWNER)
                         .build();
    }

    private StoreEntity createTestStore() {
        return StoreEntity.builder()
                          .name("테스트 매장")
                          .address("서울시 강남구 테헤란로 123")
                          .detailAddress("4층")
                          .phoneNumber("02-1234-5678")
                          .openTime(LocalTime.of(9, 0))
                          .closeTime(LocalTime.of(21, 0))
                          .build();
    }

    private OrderEntity createTestOrder(StoreEntity store) {
        return OrderEntity.builder()
                          .store(store)
                          .userId(1)
                          .orderNumber("TEST-ORDER-001")
                          .pickupTime(LocalDateTime.now().plusHours(1))
                          .needDisposables(false)
                          .build();
    }

    private PaymentEntity createTestPayment(UserEntity user) {
        return PaymentEntity.builder()
                            .user(user)
                            .title("테스트 결제")
                            .content("테스트 결제 내용")
                            .paymentMethod(PaymentEntity.PaymentMethod.CREDIT_CARD)
                            .totalAmount(10000L)
                            .build();
    }
}
