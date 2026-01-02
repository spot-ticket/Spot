package com.example.Spot.menu.domain.entity;

import java.util.UUID;

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

import com.example.Spot.global.common.UpdateBaseEntity;

@Entity
@Getter
@Table(name = "p_menu_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOptionEntity extends UpdateBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")     // DB 테이블에 생길 컬럼 이름
    private MenuEntity menu;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String detail;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Builder
    public MenuOptionEntity(MenuEntity menu, String name, String detail, int price) {
        this.menu = menu;
        this.name = name;
        this.detail = detail;
        this.price = price;
    }

    public void updateOption(String name, int price, String detail) {
        this.name = name;
        this.price = price;
        this.detail = detail;
    }

    public void changeAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
