package com.example.Spot.cart.domain.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;
import com.example.Spot.menu.domain.entity.MenuEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_cart_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemOptionEntity> options = new ArrayList<>();

    @Builder
    public CartItemEntity(MenuEntity menu, Integer quantity) {
        if (menu == null) {
            throw new IllegalArgumentException("메뉴는 필수입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        
        this.menu = menu;
        this.quantity = quantity;
    }

    public void addOption(CartItemOptionEntity option) {
        if (option == null) {
            throw new IllegalArgumentException("장바구니 옵션은 null일 수 없습니다.");
        }
        this.options.add(option);
        option.setCartItem(this);
    }

    public void updateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    protected void setCart(CartEntity cart) {
        this.cart = cart;
    }

    public Integer getSubtotal() {
        int basePrice = menu.getPrice() * quantity;
        int optionsPrice = options.stream()
            .mapToInt(opt -> opt.getMenuOption().getPrice())
            .sum() * quantity;
        return basePrice + optionsPrice;
    }
    
    // 장바구니 → 주문 전환 시 사용
    public String getMenuName() {
        return menu.getName();
    }
    
    public Integer getMenuPrice() {
        return menu.getPrice();
    }
    
    public boolean isMenuAvailable() {
        return menu.getIsAvailable();
    }
    
    public boolean isMenuHidden() {
        return menu.getIsHidden();
    }
    
    public boolean isOrderable() {
        return menu.getIsAvailable() && !menu.getIsHidden();
    }
    
    // 주문 전환 시 검증
    public void validateForOrder() {
        if (!menu.getIsAvailable()) {
            throw new IllegalStateException("품절된 메뉴입니다: " + menu.getName());
        }
        if (menu.getIsHidden()) {
            throw new IllegalStateException("판매 중지된 메뉴입니다: " + menu.getName());
        }
        
        for (CartItemOptionEntity option : options) {
            if (!option.getMenuOption().getIsAvailable()) {
                throw new IllegalStateException("선택할 수 없는 옵션입니다: " + option.getOptionName());
            }
        }
    }
}
