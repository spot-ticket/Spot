package com.example.spotuser.user.domain.repository;

import com.example.Spot.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // 중복 체크
    boolean existsByUsername(String username);
    //boolean existsByEmail(String email);

    // 조회
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findRoleById(Integer id);

    Optional<UserEntity> findById(Integer id);
    //UserEntity findByEmail(String email);
    
    // 검색한 닉네임을 포함하는 유저 정보 조회
    List<UserEntity> findByNicknameContaining(String nickname);
}

