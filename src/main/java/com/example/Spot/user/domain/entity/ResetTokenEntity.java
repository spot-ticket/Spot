package com.example.Spot.user.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_reset_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResetTokenEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_auth_id", nullable = false)
    private UUID authId;

    @Column(name = "reset_token_hash", nullable = false, unique = true)
    private String resetTokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    protected ResetTokenEntity(UUID authId, String resetTokenHash, LocalDateTime expiresAt) {
        this.authId = authId;
        this.resetTokenHash = resetTokenHash;
        this.expiresAt = expiresAt;
    }

    public static ResetTokenEntity issue(UUID authId, String resetTokenHash, long ttlMinutes) {
        return new ResetTokenEntity(
                authId,
                resetTokenHash,
                LocalDateTime.now().plusMinutes(ttlMinutes)
        );
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
