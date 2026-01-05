package com.example.Spot.order.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;

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
@Table(name = "p_order_item_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemOptionEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_id", nullable = false)
    private MenuOptionEntity menuOption;

    @Column(name = "option_name", nullable = false, length = 50)
    private String optionName;

    @Column(name = "option_detail", length = 50)
    private String optionDetail;

    @Column(name = "option_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal optionPrice;

    @Builder
    public OrderItemOptionEntity(MenuOptionEntity menuOption) {
        if (menuOption == null) {
            throw new IllegalArgumentException("메뉴 옵션은 필수입니다.");
        }
        
        this.menuOption = menuOption;
        this.optionName = menuOption.getName();
        this.optionDetail = menuOption.getDetail();
        this.optionPrice = BigDecimal.valueOf(menuOption.getPrice());
    }

    protected void setOrderItem(OrderItemEntity orderItem) {
        this.orderItem = orderItem;
    }
}

