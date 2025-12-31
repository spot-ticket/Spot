package com.example.Spot.user.domain.repository;

import com.example.Spot.user.domain.entity.UserAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, UUID> {


    boolean existsByUser_Id(UUID userId);

    // 로그인용: username으로 auth 조회
    Optional<UserAuthEntity> findByUser_Username(String username);
}
