package com.example.Spot.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

@DisplayName("OrderItemEntity 테스트")
class OrderItemEntityTest {

    @Nested
    @DisplayName("주문 항목 생성")
    class CreateOrderItem {

        @Test
        @DisplayName("Builder로 주문 항목을 생성할 수 있다")
        void createOrderItemWithBuilder() {
            // given
            OrderEntity order = createTestOrder();
            MenuEntity menu = createTestMenu();
            BigDecimal menuPrice = new BigDecimal("15000.00");
            int quantity = 2;

            // when
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .menu(menu)
                    .menuPrice(menuPrice)
                    .quantity(quantity)
                    .build();

            // then
            assertThat(orderItem.getOrder()).isEqualTo(order);
            assertThat(orderItem.getMenu()).isEqualTo(menu);
            assertThat(orderItem.getMenuPrice()).isEqualByComparingTo(menuPrice);
            assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("주문 항목의 총 가격을 계산할 수 있다")
        void calculateTotalPrice() {
            // given
            BigDecimal menuPrice = new BigDecimal("10000.00");
            int quantity = 3;
            
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(createTestOrder())
                    .menu(createTestMenu())
                    .menuPrice(menuPrice)
                    .quantity(quantity)
                    .build();

            // when
            BigDecimal totalPrice = orderItem.getMenuPrice()
                    .multiply(new BigDecimal(orderItem.getQuantity()));

            // then
            assertThat(totalPrice).isEqualByComparingTo(new BigDecimal("30000.00"));
        }
    }

    @Nested
    @DisplayName("주문 항목과 주문의 관계")
    class OrderItemOrderRelation {

        @Test
        @DisplayName("주문 항목은 주문을 참조해야 한다")
        void orderItemShouldReferenceOrder() {
            // given
            OrderEntity order = createTestOrder();

            // when
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .menu(createTestMenu())
                    .menuPrice(new BigDecimal("10000"))
                    .quantity(1)
                    .build();

            // then
            assertThat(orderItem.getOrder()).isNotNull();
            assertThat(orderItem.getOrder()).isEqualTo(order);
        }

        @Test
        @DisplayName("같은 주문에 여러 항목이 속할 수 있다")
        void multipleItemsCanBelongToSameOrder() {
            // given
            OrderEntity order = createTestOrder();

            // when
            OrderItemEntity item1 = createOrderItem(order, new BigDecimal("10000"), 1);
            OrderItemEntity item2 = createOrderItem(order, new BigDecimal("15000"), 2);

            // then
            assertThat(item1.getOrder()).isEqualTo(order);
            assertThat(item2.getOrder()).isEqualTo(order);
            assertThat(item1.getOrder()).isEqualTo(item2.getOrder());
        }
    }

    // 테스트 헬퍼 메서드
    private OrderEntity createTestOrder() {
        return OrderEntity.builder()
                .store(createTestStore())
                .userId(1L)
                .orderNumber("ORD-001")
                .pickupTime(java.time.LocalDateTime.now().plusHours(1))
                .build();
    }

    private StoreEntity createTestStore() {
        // OrderItemEntity 테스트에서는 StoreEntity의 세부 구현은 중요하지 않음
        return null;
    }

    private MenuEntity createTestMenu() {
        return MenuEntity.builder()
                .name("테스트 메뉴")
                .price(10000)
                .build();
    }

    private OrderItemEntity createOrderItem(OrderEntity order, BigDecimal price, int quantity) {
        return OrderItemEntity.builder()
                .order(order)
                .menu(createTestMenu())
                .menuPrice(price)
                .quantity(quantity)
                .build();
    }
}
