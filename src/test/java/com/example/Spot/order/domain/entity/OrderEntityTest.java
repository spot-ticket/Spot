package com.example.Spot.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.store.domain.entity.StoreEntity;

public class OrderEntityTest {

    @Test
    void 주문이_정상적인_흐름으로_진행되면_각_단계별_상태와_시간이_기록된다() {
        StoreEntity store = StoreEntity.builder()
                .name("S")
                .roadAddress("R")
                .build();

        OrderEntity order = OrderEntity.builder()
                .store(store)
                .userId(1)
                .orderNumber("ON-1")
                .request("no")
                .needDisposables(false)
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);

        order.completePayment();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPaymentCompletedAt()).isNotNull();

        order.acceptOrder(15);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(order.getAcceptedAt()).isNotNull();
        assertThat(order.getEstimatedTime()).isEqualTo(15);

        order.startCooking();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COOKING);
        assertThat(order.getCookingStartedAt()).isNotNull();

        order.readyForPickup();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.READY);
        assertThat(order.getCookingCompletedAt()).isNotNull();

        order.completeOrder();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getPickedUpAt()).isNotNull();
    }

    @Test
    void 잘못된_상태에서_완료_처리를_하면_예외가_발생한다() {
        StoreEntity store = StoreEntity.builder().name("S").roadAddress("R").build();
        OrderEntity order = OrderEntity.builder()
                .store(store)
                .userId(2)
                .orderNumber("ON-2")
                .request(null)
                .needDisposables(false)
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        assertThatThrownBy(order::completeOrder)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void 주문을_취소하면_취소_상태로_변경되고_취소_시간이_기록된다() {
        StoreEntity store = StoreEntity.builder().name("S").roadAddress("R").build();
        OrderEntity order = OrderEntity.builder()
                .store(store)
                .userId(2)
                .orderNumber("ON-2")
                .request(null)
                .needDisposables(false)
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        order.cancelOrder("bye", CancelledBy.CUSTOMER);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelledAt()).isNotNull();
        assertThat(order.getReason()).isEqualTo("bye");
    }
}
