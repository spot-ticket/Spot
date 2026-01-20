package com.example.Spot.store.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreCategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

public interface StoreCategoryRepository extends JpaRepository<StoreCategoryEntity, UUID> {

    // Category - 카테고리 별 매장 조회
    @Query("""
        select scm
        from StoreCategoryEntity scm
        join fetch scm.store s
        where scm.category.id  = :categoryId
          and s.isDeleted = false
          and s.status = 'APPROVED'
    """)
    List<StoreCategoryEntity> findAllActiveByCategoryIdWithStore(@Param("categoryId") UUID categoryId);

    // Store
    List<StoreCategoryEntity> findByCategoryAndIsDeletedFalse(CategoryEntity category);

    List<StoreCategoryEntity> findByStoreAndIsDeletedFalse(StoreEntity store);

    // Test
    Optional<StoreCategoryEntity> findByStoreAndCategoryAndIsDeletedFalse(
            StoreEntity store,
            CategoryEntity category
    );

    List<StoreCategoryEntity> findAllByStore_Id(UUID storeId);

    long countByCategoryAndIsDeletedFalse(CategoryEntity category);
}
