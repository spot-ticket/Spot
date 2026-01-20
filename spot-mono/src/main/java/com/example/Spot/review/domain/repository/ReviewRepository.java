package com.example.Spot.review.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.review.domain.entity.ReviewEntity;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    // 특정 가게의 리뷰 조회 (삭제되지 않은 리뷰만)
    @Query("SELECT r FROM ReviewEntity r " +
            "LEFT JOIN FETCH r.user u " +
            "WHERE r.store.id = :storeId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    Page<ReviewEntity> findByStoreIdAndIsDeletedFalse(@Param("storeId") UUID storeId, Pageable pageable);

    // 특정 가게의 전체 리뷰 (삭제된 것 포함, 관리자용)
    @Query("SELECT r FROM ReviewEntity r " +
            "LEFT JOIN FETCH r.user u " +
            "WHERE r.store.id = :storeId " +
            "ORDER BY r.createdAt DESC")
    Page<ReviewEntity> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    // 특정 사용자의 리뷰 조회
    @Query("SELECT r FROM ReviewEntity r " +
            "LEFT JOIN FETCH r.store s " +
            "WHERE r.user.id = :userId " +
            "AND r.isDeleted = false " +
            "ORDER BY r.createdAt DESC")
    List<ReviewEntity> findByUserIdAndIsDeletedFalse(@Param("userId") Integer userId);

    // 리뷰 상세 조회 (작성자/관리자 확인용)
    @Query("SELECT r FROM ReviewEntity r " +
            "LEFT JOIN FETCH r.user u " +
            "LEFT JOIN FETCH r.store s " +
            "WHERE r.id = :reviewId " +
            "AND r.isDeleted = false")
    Optional<ReviewEntity> findByIdWithDetails(@Param("reviewId") UUID reviewId);

    // 가게의 평균 별점 계산
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r " +
            "WHERE r.store.id = :storeId " +
            "AND r.isDeleted = false")
    Double getAverageRatingByStoreId(@Param("storeId") UUID storeId);

    // 가게의 리뷰 개수
    @Query("SELECT COUNT(r) FROM ReviewEntity r " +
            "WHERE r.store.id = :storeId " +
            "AND r.isDeleted = false")
    Long countByStoreIdAndIsDeletedFalse(@Param("storeId") UUID storeId);
}
