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

    // [추가] 손님에게 보여줄 상세 설명
    @Column(length = 255)
    private String description;

    // [추가] 메뉴 이미지 URL
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    // [추가] DTO 변환을 위한 양방향 매핑 (DB에는 영향 없음)
    // mappedBy = "menu"는 MenuOptionEntity의 'menu' 변수명을 가리킴
    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    private List<MenuOptionEntity> options = new ArrayList<>();

    @Builder
    public MenuEntity(StoreEntity store, String name, String category, int price, String description, String imageUrl) {
        this.store = store;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void updateMenu(String name, int price, String category, String description, String imageUrl) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void changeAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void changeHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}
