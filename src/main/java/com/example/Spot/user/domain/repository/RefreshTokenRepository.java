package com.example.Spot.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.user.domain.entity.RefreshTokenEntity;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByRefreshTokenHash(String refreshTokenHash);
}
