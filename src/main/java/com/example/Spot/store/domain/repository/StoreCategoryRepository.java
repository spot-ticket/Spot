package com.example.Spot.store.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreCategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategoryEntity, UUID> {

    // 카테고리에 매핑된 (삭제되지 않은) 매장 목록 조회용
    List<StoreCategoryEntity> findByCategoryAndIsDeletedFalse(CategoryEntity category);

    // store 기준으로 매핑 조회가 필요할 때
    List<StoreCategoryEntity> findByStoreAndIsDeletedFalse(StoreEntity store);

    // 중복 매핑 방지/검증용 (store-category 1쌍)
    Optional<StoreCategoryEntity> findByStoreAndCategoryAndIsDeletedFalse(StoreEntity store, CategoryEntity category);

    // 특정 가게에 연결된 모든 카테고리 맵 조회
    List<StoreCategoryEntity> findAllByStore_Id(UUID storeId);

    // 카테고리 내 매핑 개수 확인 등
    long countByCategoryAndIsDeletedFalse(CategoryEntity category);
}
