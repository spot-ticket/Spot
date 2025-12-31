package com.example.Spot.store.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
//@Table(name = "p_store_category")
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

    public void updateName(String name) {
        this.name = name;
    }


}
