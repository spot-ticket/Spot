package com.example.Spot.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

public class OrderItemEntityTest {

    @Test
    void 주문항목을_생성하고_옵션을_추가하면_정상적으로_연결된다() {
        StoreEntity store = StoreEntity.builder().name("S").roadAddress("R").build();

        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name("Tea")
                .category("Drink")
                .price(3000)
                .build();

        OrderItemEntity item = OrderItemEntity.builder()
                .menu(menu)
                .quantity(2)
                .build();

        assertThat(item.getMenuName()).isEqualTo("Tea");
        assertThat(item.getQuantity()).isEqualTo(2);

        MenuOptionEntity option = MenuOptionEntity.builder()
                .menu(menu)
                .name("Sugar")
                .price(0)
                .build();

        OrderItemOptionEntity orderOption = OrderItemOptionEntity.builder()
                .menuOption(option)
                .build();

        item.addOrderItemOption(orderOption);
        assertThat(item.getOrderItemOptions()).hasSize(1);
        assertThat(orderOption.getOptionName()).isEqualTo("Sugar");
    }

    @Test
    void 메뉴가_null이거나_수량이_0이하면_예외가_발생한다() {
        MenuEntity menu = null;
        assertThatThrownBy(() -> OrderItemEntity.builder().menu(menu).quantity(1).build())
                .isInstanceOf(IllegalArgumentException.class);

        StoreEntity store = StoreEntity.builder().name("S").roadAddress("R").build();
        MenuEntity realMenu = MenuEntity.builder().store(store).name("X").category("C").price(1000).build();

        assertThatThrownBy(() -> OrderItemEntity.builder().menu(realMenu).quantity(0).build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
