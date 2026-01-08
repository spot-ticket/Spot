package com.example.Spot.menu.domain.entity;

import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;

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

@Entity
@Getter
@Table(name = "p_menu_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOptionEntity extends UpdateBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")     // DB 테이블에 생길 컬럼 이름
    private MenuEntity menu;

    // 옵션명
    @Column(nullable = false, length = 50)
    private String name;

    // 옵션 상세 설명
    @Column(nullable = true, length = 100)
    private String detail;

    // 가격
    @Column(nullable = false)
    private Integer price;

    // 품절 여부 체크
    @Column(name = "is_available")
    private Boolean isAvailable = true;

    // 숨김 여부 체크
    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Builder
    public MenuOptionEntity(MenuEntity menu, String name, String detail, Integer price, Integer createdBy) {
        super(createdBy);
        this.menu = menu;
        this.name = name;
        this.detail = detail;
        this.price = price;
    }

    public void updateOption(String name, Integer price, String detail) {

        // 1. 이름이 들어오면 수정
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        // 2. 가격이 들어오면 수정
        if (price != null) {
            this.price = price;
        }

        // 3. 상세 설명이 들어오면 수정
        if (detail != null) {
            this.detail = detail;
        }
    }

    public void changeAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void changeHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}
