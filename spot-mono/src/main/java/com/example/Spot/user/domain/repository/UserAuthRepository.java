package com.example.Spot.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.user.domain.entity.UserAuthEntity;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, UUID> {

    boolean existsByUserId(Integer userId);

    // 로그인용: username으로 auth 조회
    Optional<UserAuthEntity> findByUserId(Integer userid);

    // 테스트용: username으로 auth 조회 (언더스코어 버전)
    Optional<UserAuthEntity> findByUser_Username(String username);
}
