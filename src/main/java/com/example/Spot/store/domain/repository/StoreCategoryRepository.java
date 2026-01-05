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

    @Query("""
        select sc
        from StoreCategoryEntity sc
        join fetch sc.store s
        where sc.isDeleted = false
          and s.isDeleted = false
          and sc.category.id = :categoryId
    """)
    List<StoreCategoryEntity> findAllActiveByCategoryIdWithStore(@Param("categoryId") UUID categoryId);

    List<StoreCategoryEntity> findByCategoryAndIsDeletedFalse(CategoryEntity category);

    List<StoreCategoryEntity> findByStoreAndIsDeletedFalse(StoreEntity store);

    Optional<StoreCategoryEntity> findByStoreAndCategoryAndIsDeletedFalse(
            StoreEntity store,
            CategoryEntity category
    );

    List<StoreCategoryEntity> findAllByStore_Id(UUID storeId);

    long countByCategoryAndIsDeletedFalse(CategoryEntity category);
}
