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

    // 메뉴명
    @Column(nullable = false, length = 50)
    private String name;

    // 카테고리
    @Column(nullable = false, length = 50)
    private String category;

    // 가격
    @Column(nullable = false)
    private Integer price;

    // 메뉴 상세 설명
    @Column(length = 255)
    private String description;

    // 메뉴 이미지 URL
    @Column(name = "image_url")
    private String imageUrl;

    // 품절 여부 체크
    @Column(name = "is_available")
    private Boolean isAvailable = true;

    // 숨김 여부 체크
    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    private List<MenuOptionEntity> options = new ArrayList<>();

    @Builder
    public MenuEntity(StoreEntity store, String name, String category, Integer price, String description, String imageUrl, List<MenuOptionEntity> options) {
        this.store = store;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.options = (options != null) ? options : new ArrayList<>();
    }

    public void updateMenu(String name, Integer price, String category, String description, String imageUrl) {

        // 1. 이름이 들어오면 수정
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        // 2. 가격이 들어오면 수정
        if (price != null) {
            this.price = price;
        }

        // 3. 카테고리가 들어오면 수정
        if (category != null && !category.isBlank()) {
            this.category = category;
        }

        // 4. 상세 설명이 들어오면 수정
        if (description != null) {
            this.description = description;
        }

        // 5. 이미지가 들어오면 수정
        if (imageUrl != null ) {
            this.imageUrl = imageUrl;
        }
    }

    public void changeAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void changeHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}
