package com.example.Spot.menu.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id")     // DB 테이블에 생길 컬럼 이름
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")     // DB 테이블에 생길 컬럼 이름
    private StoreEntity store;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 255)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "quantity")
    private Integer quantity;

    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    private List<MenuOptionEntity> options = new ArrayList<>();

    @Builder
    public MenuEntity(StoreEntity store, String name, String category, Integer price, String description, String imageUrl, Integer quantity, List<MenuOptionEntity> options) {
        this.store = store;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.quantity = (quantity != null) ? quantity : 0;
        this.options = (options != null) ? options : new ArrayList<>();
    }

    public void updateMenu(String name, Integer price, String category, String description, String imageUrl, Integer quantity) {

        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (price != null) {
            this.price = price;
        }

        if (category != null && !category.isBlank()) {
            this.category = category;
        }

        if (description != null) {
            this.description = description;
        }

        if (imageUrl != null ) {
            this.imageUrl = imageUrl;
        }

        if (quantity != null) {
            this.quantity = quantity;
        }
    }

    public void changeAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void changeHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public void decreaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소할 수량은 0보다 커야 합니다.");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.quantity -= amount;
    }

    public void increaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가할 수량은 0보다 커야 합니다.");
        }
        this.quantity += amount;
    }

    public void setQuantity(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }
}
