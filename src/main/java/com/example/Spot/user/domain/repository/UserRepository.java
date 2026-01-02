package com.example.Spot.user.domain.repository;

import com.example.Spot.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // 중복 체크
    boolean existsByUsername(String username);
    //boolean existsByEmail(String email);

    // 조회
    Optional<UserEntity> findByUsername(String username);
    //UserEntity findByEmail(String email);
}

