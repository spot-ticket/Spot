package com.example.Spot.store.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Spot.store.domain.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {

    // 삭제되지 않은 가게 전체 조회
    List<StoreEntity> findByIsDeletedFalse();

    // ID로 삭제되지 않은 가게 조회
    Optional<StoreEntity> findByIdAndIsDeletedFalse(UUID id);

    // 가게 이름에 특정 단어가 포함된 목록 검색
    List<StoreEntity> findByNameContaining(String name);

}
