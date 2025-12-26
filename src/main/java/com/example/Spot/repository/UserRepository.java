package com.example.Spot.repository;

import com.example.Spot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Boolean existsByUsername(String username);

    // get api
    Optional<UserEntity> findByUsername(String username);


}
