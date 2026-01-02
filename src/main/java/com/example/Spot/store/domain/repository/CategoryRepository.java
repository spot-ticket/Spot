package com.example.Spot.store.domain.repository;

import com.example.Spot.store.domain.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    // soft delete 컬럼(isDeleted)이 UpdateBaseEntity에 있다고 가정
    List<CategoryEntity> findByIsDeletedFalse();

    Optional<CategoryEntity> findByIdAndIsDeletedFalse(UUID id);
}
