package com.example.Spot.cart.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

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
@Table(name = "p_cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();

    @Builder
    public CartEntity(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        this.userId = userId;
    }

    public void addItem(CartItemEntity item) {
        if (item == null) {
            throw new IllegalArgumentException("장바구니 아이템은 null일 수 없습니다.");
        }
        
        // 첫 아이템이면 store 설정
        if (this.store == null && item.getMenu() != null) {
            this.store = item.getMenu().getStore();
        }
        
        this.items.add(item);
        item.setCart(this);
    }

    public void validateStore(UUID storeId) {
        if (this.store != null && !this.store.getId().equals(storeId)) {
            throw new IllegalArgumentException(
                "다른 가게의 상품입니다. 현재 장바구니: " + store.getName()
            );
        }
    }

    public void clear() {
        this.items.clear();
        this.store = null;
    }

    public List<CartItemEntity> getActiveItems() {
        return new ArrayList<>(items);
    }
}
