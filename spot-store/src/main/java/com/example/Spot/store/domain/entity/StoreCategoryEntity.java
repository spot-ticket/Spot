package com.example.spotstore.store.domain.entity;

import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_store_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreCategoryEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Builder
    public StoreCategoryEntity(StoreEntity store, CategoryEntity category) {
        this.store = store;
        this.category = category;
    }

}
