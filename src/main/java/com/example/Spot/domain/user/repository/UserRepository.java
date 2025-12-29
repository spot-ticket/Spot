package com.example.Spot.domain.user.repository;

import com.example.Spot.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Boolean existsByUsername(String username);

    // 조회 쿼리
    UserEntity findByUsername(String username);
}
