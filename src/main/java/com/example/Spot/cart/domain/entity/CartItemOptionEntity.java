package com.example.Spot.cart.domain.entity;

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
@Table(name = "p_cart_item_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemOptionEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", nullable = false)
    private CartItemEntity cartItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_id", nullable = false)
    private MenuOptionEntity menuOption;

    @Builder
    public CartItemOptionEntity(MenuOptionEntity menuOption) {
        if (menuOption == null) {
            throw new IllegalArgumentException("메뉴 옵션은 필수입니다.");
        }
        
        this.menuOption = menuOption;
    }
    
    public String getOptionName() {
        return menuOption.getName();
    }
    
    public String getOptionDetail() {
        return menuOption.getDetail();
    }
    
    public Integer getOptionPrice() {
        return menuOption.getPrice();
    }

    protected void setCartItem(CartItemEntity cartItem) {
        this.cartItem = cartItem;
    }
}

