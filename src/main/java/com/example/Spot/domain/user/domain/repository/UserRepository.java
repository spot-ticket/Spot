package com.example.Spot.domain.user.domain.repository;

import com.example.Spot.domain.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // 중복 체크
    boolean existsByUsername(String username);
    //boolean existsByEmail(String email);

    // 조회
    UserEntity findByUsername(String username);
    //UserEntity findByEmail(String email);
}

