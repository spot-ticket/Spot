package com.example.Spot.menu.domain.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.global.common.UpdateBaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Builder;

import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="p_origin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OriginEntity extends UpdateBaseEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="menu_id", nullable=false)
    private MenuEntity menu;

    @Column(name="origin_name", nullable=false, length=100)
    private String originName;

    @Column(name="ingredient_name", nullable=false, length=100)
    private String ingredientName;

    @Builder
    public OriginEntity(MenuEntity menu, String origiinName, String ingredientName){
        validateNames(originName, ingredientName);

        this.menu = menu;
        this.originName = originName;
        this.ingredientName = ingredientName;
    }

    public void updateInfo(String originName, String ingredientName){
        validateNames(originName, ingredientName);

        if (originName != null && !originName.isBlank()) {
            this.originName = originName;
        }
        if (ingredientName != null && !ingredientName.isBlank()) {
            this.ingredientName = ingredientName;
        }
    }

    private void validateNames(String originName, String ingredientName) {
        boolean isOriginBlank = (originName == null || originName.isBlank());
        boolean isIngredientBlank = (ingredientName == null || ingredientName.isBlank());

        if (isOriginBlank && isIngredientBlank) {
            throw new IllegalArgumentException("원산지 이름과 재료 이름 중 적어도 하나는 입력되어야 합니다.");
        }
    }
}
}