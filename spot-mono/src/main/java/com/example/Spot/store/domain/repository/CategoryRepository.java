package com.example.Spot.store.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Spot.store.domain.entity.CategoryEntity;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    // main
    List<CategoryEntity> findAllByIsDeletedFalse();

    Optional<CategoryEntity> findByIdAndIsDeletedFalse(UUID id);
    Optional<CategoryEntity> findByNameAndIsDeletedFalse(String name);

    boolean existsByNameAndIsDeletedFalse(String name);

    // test
    Optional<CategoryEntity> findByName(String name);
    // soft delete 컬럼(isDeleted)이 UpdateBaseEntity에 있다고 가정
    List<CategoryEntity> findByIsDeletedFalse();
}
