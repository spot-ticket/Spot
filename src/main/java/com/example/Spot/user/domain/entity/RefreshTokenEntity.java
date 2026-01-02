package com.example.Spot.user.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auth_id", nullable = false)
    private UserAuthEntity auth;

    @Column(name = "refresh_token_hash", nullable = false, length = 128, unique = true)
    private String refreshTokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public boolean isActive(LocalDateTime now) {
        return !getIsDeleted()
                && revokedAt == null
                && expiresAt.isAfter(now);
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
        softDelete();
    }
}
