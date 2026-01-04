package com.example.Spot.order.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.menu.domain.entity.MenuEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "menu_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal menuPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder
    public OrderItemEntity(MenuEntity menu, BigDecimal menuPrice, Integer quantity) {
        if (menu == null) {
            throw new IllegalArgumentException("메뉴는 필수입니다.");
        }
        if (menuPrice == null || menuPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("메뉴 가격은 0 이상이어야 합니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        
        this.menu = menu;
        this.menuName = menu.getName();
        this.menuPrice = menu.getPrice();
        this.quantity = quantity;
    }

    // 양방향 관계 설정을 위한 메서드
    protected void setOrder(OrderEntity order) {
        this.order = order;
    }
}
