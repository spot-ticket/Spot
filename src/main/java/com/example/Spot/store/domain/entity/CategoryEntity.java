package com.example.Spot.store.domain.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity extends UpdateBaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "category", 
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private final Set<StoreCategoryEntity> storeCategoryMaps = new HashSet<>();
    
    @Column(nullable = false)
    private String name;
    // 도메인 메서드

    @Builder
    public CategoryEntity(String name) {
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }
    
}
