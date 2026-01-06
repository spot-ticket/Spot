package com.example.Spot.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.user.domain.entity.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // 중복 체크
    boolean existsByUsername(String username);
    //boolean existsByEmail(String email);

    // 조회
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findRoleById(Integer id);

    Optional<UserEntity> findById(Integer id);
    //UserEntity findByEmail(String email);
}

