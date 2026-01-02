package com.example.Spot.menu.domain.entity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")     // DB 테이블에 생길 컬럼 이름
    private StoreEntity store;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Builder
    public MenuEntity(StoreEntity store, String name, String category, int price) {
        this.store = store;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public void updateMenu(String name, int price, String category){
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public void changeAvailable(Boolean isAvailable){
        this.isAvailable = isAvailable;
    }

    public void changeHidden(Boolean isHidden){
        this.isHidden = isHidden;
    }
}
