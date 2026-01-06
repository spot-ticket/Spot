package com.example.Spot.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.user.domain.entity.ResetTokenEntity;

public interface ResetTokenRepository extends JpaRepository<ResetTokenEntity, UUID> {
    Optional<ResetTokenEntity> findByResetTokenHash(String resetTokenHash);
    void deleteByAuthId(UUID authId);
}
