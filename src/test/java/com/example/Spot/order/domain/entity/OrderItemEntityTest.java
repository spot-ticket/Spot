package com.example.Spot.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class OrderItemEntityTest {

    @Test
    void 주문항목을_생성하고_옵션을_추가하면_정상적으로_연결된다() {
        OrderItemEntity item = OrderItemEntity.builder()
                .menuId(java.util.UUID.randomUUID())
                .menuName("Tea")
                .menuPrice(java.math.BigDecimal.valueOf(3000))
                .quantity(2)
                .build();

        assertThat(item.getMenuName()).isEqualTo("Tea");
        assertThat(item.getQuantity()).isEqualTo(2);

        OrderItemOptionEntity orderOption = OrderItemOptionEntity.builder()
                .menuOptionId(java.util.UUID.randomUUID())
                .optionName("Sugar")
                .optionDetail(null)
                .optionPrice(java.math.BigDecimal.ZERO)
                .build();

        item.addOrderItemOption(orderOption);
        assertThat(item.getOrderItemOptions()).hasSize(1);
        assertThat(orderOption.getOptionName()).isEqualTo("Sugar");
    }

    @Test
    void 메뉴가_null이거나_수량이_0이하면_예외가_발생한다() {
        assertThatThrownBy(() -> OrderItemEntity.builder()
                .menuId(null)
                .menuName("Test")
                .menuPrice(java.math.BigDecimal.valueOf(1000))
                .quantity(1)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> OrderItemEntity.builder()
                .menuId(java.util.UUID.randomUUID())
                .menuName("X")
                .menuPrice(java.math.BigDecimal.valueOf(1000))
                .quantity(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
