package com.example.Spot.order.domain.entity;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderEntity 테스트")
class OrderEntityTest {

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("Builder로 주문을 생성하면 초기 상태가 PENDING이어야 한다")
        void createOrderWithBuilder() {
            // given
            StoreEntity store = createTestStore();
            LocalDateTime pickupTime = LocalDateTime.now().plusHours(1);

            // when
            OrderEntity order = OrderEntity.builder()
                    .store(store)
                    .userId(1L)
                    .orderNumber("ORD-20250101-001")
                    .request("덜 맵게 해주세요")
                    .needDisposables(true)
                    .pickupTime(pickupTime)
                    .build();

            // then
            assertThat(order.getStore()).isEqualTo(store);
            assertThat(order.getUserId()).isEqualTo(1L);
            assertThat(order.getOrderNumber()).isEqualTo("ORD-20250101-001");
            assertThat(order.getRequest()).isEqualTo("덜 맵게 해주세요");
            assertThat(order.getNeedDisposables()).isTrue();
            assertThat(order.getPickupTime()).isEqualTo(pickupTime);
            assertThat(order.getOrderStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
            assertThat(order.getOrderItems()).isEmpty();
        }

        @Test
        @DisplayName("needDisposables가 null이면 false로 설정되어야 한다")
        void createOrderWithNullNeedDisposables() {
            // given
            StoreEntity store = createTestStore();

            // when
            OrderEntity order = OrderEntity.builder()
                    .store(store)
                    .userId(1L)
                    .orderNumber("ORD-001")
                    .needDisposables(null)
                    .pickupTime(LocalDateTime.now())
                    .build();

            // then
            assertThat(order.getNeedDisposables()).isFalse();
        }

        @Test
        @DisplayName("생성 시 orderItems는 빈 리스트로 초기화되어야 한다")
        void createOrderWithEmptyOrderItems() {
            // given
            StoreEntity store = createTestStore();

            // when
            OrderEntity order = OrderEntity.builder()
                    .store(store)
                    .userId(1L)
                    .orderNumber("ORD-001")
                    .pickupTime(LocalDateTime.now())
                    .build();

            // then
            assertThat(order.getOrderItems()).isNotNull();
            assertThat(order.getOrderItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("주문 항목 추가")
    class AddOrderItem {

        @Test
        @DisplayName("주문에 주문 항목을 추가할 수 있다")
        void addOrderItem() {
            // given
            OrderEntity order = createTestOrder();
            MenuEntity menu = createTestMenu();
            
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .menu(menu)
                    .menuPrice(new BigDecimal("15000.00"))
                    .quantity(2)
                    .build();

            // when
            order.getOrderItems().add(orderItem);

            // then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getOrderItems().get(0)).isEqualTo(orderItem);
        }

        @Test
        @DisplayName("여러 개의 주문 항목을 추가할 수 있다")
        void addMultipleOrderItems() {
            // given
            OrderEntity order = createTestOrder();
            
            OrderItemEntity item1 = createTestOrderItem(order, new BigDecimal("10000"), 1);
            OrderItemEntity item2 = createTestOrderItem(order, new BigDecimal("15000"), 2);
            OrderItemEntity item3 = createTestOrderItem(order, new BigDecimal("8000"), 3);

            // when
            order.getOrderItems().add(item1);
            order.getOrderItems().add(item2);
            order.getOrderItems().add(item3);

            // then
            assertThat(order.getOrderItems()).hasSize(3);
        }
    }

    // 테스트 헬퍼 메서드
    private StoreEntity createTestStore() {
        // OrderEntity 테스트에서는 StoreEntity의 세부 구현은 중요하지 않으므로 null 사용
        return null;
    }

    private OrderEntity createTestOrder() {
        return OrderEntity.builder()
                .store(createTestStore())
                .userId(1L)
                .orderNumber("ORD-001")
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();
    }

    private MenuEntity createTestMenu() {
        return MenuEntity.builder()
                .name("테스트 메뉴")
                .price(10000)
                .build();
    }

    private OrderItemEntity createTestOrderItem(OrderEntity order, BigDecimal price, int quantity) {
        return OrderItemEntity.builder()
                .order(order)
                .menu(createTestMenu())
                .menuPrice(price)
                .quantity(quantity)
                .build();
    }
}
