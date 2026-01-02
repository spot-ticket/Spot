package com.example.Spot.store.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "p_store_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity extends UpdateBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<StoreViewEntity> storeCategoryMaps = new HashSet<>();

    // 도메인 메서드

    @Builder
    public CategoryEntity(String name) {
        this.name = name;
    }

    @Builder
    public void updateName(String name) {
        this.name = name;
    }


}
