package com.example.Spot.menu.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "p_menu_option")
@Getter
@EntityListeners(AuditingEntityListener.class)
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

    public void updateOption(String name, int price, String detail){
        this.name = name;
        this.price = price;
        this.detail = detail;
    }

    public void changeAvailable(Boolean isAvailable){
        this.isAvailable = isAvailable;
    }
}