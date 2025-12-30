package com.example.Spot.menu.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.Spot.menu.domain.entity.MenuEntity;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Getter
@Table(name="p_origin")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OriginEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="menu_id")
    private MenuEntity menu;

    @Column(name="origin_name", nullable=false)
    private String originName;

    @Column(name="ingredient_name", nullable=false)
    private String ingredientName;

    @Column(name="is_deleted", nullable=false)
    private Boolean isDeleted=false;

    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public OriginEntity(MenuEntity menu, String origiinName, String ingredientName){
        this.menu = menu;
        this.originName = originName;
        this.ingredientName = ingredientName;
        this.isDeleted = false;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void updateInfo(String originName, String ingredientName){
        this.originName = originName;
        this.ingredientName = ingredientName;
    }
}