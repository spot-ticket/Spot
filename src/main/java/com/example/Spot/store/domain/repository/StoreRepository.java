package com.example.Spot.store.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.store.domain.entity.StoreEntity;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {

    // 기본 조회
    @Query("SELECT s FROM StoreEntity s " +
            "LEFT JOIN FETCH s.storeCategoryMaps sc " +
            "LEFT JOIN FETCH sc.category " +
            "WHERE (:isAdmin = true OR (s.isDeleted = false AND s.status = 'APPROVED'))")
    Page<StoreEntity> findAllByRole(@Param("isAdmin") boolean isAdmin, Pageable pageable);
    
    // 상세 조회: or을 통해 권한 혹은 소프트제거 여부에 따른 조회범위 설정
    @Query("SELECT s FROM StoreEntity s " +
            "LEFT JOIN FETCH s.storeCategoryMaps sc " +
            "LEFT JOIN FETCH sc.category " +
            "LEFT JOIN FETCH s.users su " +
            "LEFT JOIN FETCH su.user u " +
            "WHERE s.id = :id " +
            "AND (:isAdmin = true OR (s.isDeleted = false AND s.status = 'APPROVED'))")
    Optional<StoreEntity> findByIdWithDetails(@Param("id") UUID id, @Param("isAdmin") boolean isAdmin);

    // 검색 기능
    @Query("SELECT DISTINCT s FROM StoreEntity s " +
            "LEFT JOIN FETCH s.storeCategoryMaps sc " +
            "LEFT JOIN FETCH sc.category " +
            "WHERE s.name LIKE %:keyword% " +
            "AND (:isAdmin = true OR (s.isDeleted = false AND s.status = 'APPROVED'))")
    Page<StoreEntity> searchByName(@Param("keyword") String keyword, @Param("isAdmin") boolean isAdmin, Pageable pageable);

    // 특정 유저가 담당하는 매장 조회 (중간 테이블 Join)
    // Owner/Chef는 자신의 모든 가게를 조회 (PENDING, APPROVED, REJECTED 모두 포함)
    @Query("SELECT DISTINCT s FROM StoreEntity s " +
            "LEFT JOIN FETCH s.storeCategoryMaps sc " +
            "LEFT JOIN FETCH sc.category " +
            "JOIN s.users su " +
            "JOIN su.user u " +
            "WHERE u.id = :userId " +
            "AND u.role IN ('OWNER', 'CHEF') " +
            "AND s.isDeleted = false")
    List<StoreEntity> findAllByOwnerId(@Param("userId") Integer userId);


    // category-repo
    // 삭제되지 않은 가게 전체 조회
    List<StoreEntity> findByIsDeletedFalse();

    // ID로 삭제되지 않은 가게 조회
    Optional<StoreEntity> findByIdAndIsDeletedFalse(UUID id);

}
