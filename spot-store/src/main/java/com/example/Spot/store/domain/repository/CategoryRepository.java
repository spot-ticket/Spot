package com.example.Spot.store.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.example.Spot.store.domain.entity.CategoryEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    // main
    List<CategoryEntity> findAllByIsDeletedFalse();

    Optional<CategoryEntity> findByIdAndIsDeletedFalse(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CategoryEntity c WHERE c.name = :name AND c.isDeleted = false")
    Optional<CategoryEntity> findByNameAndIsDeletedFalseWithLock(@Param("name") String name);

    boolean existsByNameAndIsDeletedFalse(String name);

    // test
    Optional<CategoryEntity> findByName(String name);
    // soft delete 컬럼(isDeleted)이 UpdateBaseEntity에 있다고 가정
    List<CategoryEntity> findByIsDeletedFalse();
}
