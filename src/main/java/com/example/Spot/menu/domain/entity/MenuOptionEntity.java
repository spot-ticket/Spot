package com.example.Spot.menu.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_menu_option")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class MenuOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
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

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    public void delete(){
        this.isDeleted = true;
    }
}