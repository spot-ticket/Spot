package com.example.Spot.store.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_store_view")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreViewEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_category_id", nullable = false)
    private CategoryEntity category;

    @Builder
    public StoreViewEntity(StoreEntity store, CategoryEntity category) {
        this.store = store;
        this.category = category;
    }


}
