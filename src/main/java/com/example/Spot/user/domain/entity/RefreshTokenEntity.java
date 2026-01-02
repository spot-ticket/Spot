package com.example.Spot.user.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.Spot.global.common.UpdateBaseEntity;

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

    @Builder
    public RefreshTokenEntity(UserAuthEntity auth, String refreshTokenHash, LocalDateTime expiresAt){
        this.auth = auth;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
    }

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
