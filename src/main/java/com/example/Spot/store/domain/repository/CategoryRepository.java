package com.example.Spot.store.domain.repository;

import com.example.Spot.store.domain.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // soft delete 컬럼(isDeleted)이 UpdateBaseEntity에 있다고 가정
    List<CategoryEntity> findByIsDeletedFalse();

    Optional<CategoryEntity> findByIdAndIsDeletedFalse(Long id);
}
