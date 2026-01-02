package com.example.Spot.store.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Spot.store.domain.entity.StoreUserEntity;

@Repository
public interface StoreUserRepository extends JpaRepository<StoreUserEntity, UUID> {

    // 특정 유저(ID)가 속한 모든 가게 매핑 정보 조회
    List<StoreUserEntity> findAllByUser_Id(UUID userId);
}
