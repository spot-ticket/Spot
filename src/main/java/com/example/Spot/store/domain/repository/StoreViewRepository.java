package com.example.Spot.store.domain.repository;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.entity.StoreViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreViewRepository extends JpaRepository<StoreViewEntity, UUID> {

    // 카테고리에 매핑된 (삭제되지 않은) 매장 목록 조회용
    List<StoreViewEntity> findByCategoryAndIsDeletedFalse(CategoryEntity category);

    // store 기준으로 매핑 조회가 필요할 때
    List<StoreViewEntity> findByStoreAndIsDeletedFalse(StoreEntity store);

    // 중복 매핑 방지/검증용 (store-category 1쌍)
    Optional<StoreViewEntity> findByStoreAndCategoryAndIsDeletedFalse(StoreEntity store, CategoryEntity category);

    // 카테고리 내 매핑 개수 확인 등
    long countByCategoryAndIsDeletedFalse(CategoryEntity category);
}
