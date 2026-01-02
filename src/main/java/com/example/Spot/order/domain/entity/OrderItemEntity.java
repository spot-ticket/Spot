package com.example.Spot.order.domain.entity;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.menu.domain.entity.MenuEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;

    @Column(name = "menu_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal menuPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder
    public OrderItemEntity(OrderEntity order, MenuEntity menu,
                          BigDecimal menuPrice, Integer quantity) {
        this.order = order;
        this.menu = menu;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
    }
}
