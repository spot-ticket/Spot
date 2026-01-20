package com.example.Spot.user.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auth_id", nullable = false)
    private UserAuthEntity auth;

    @Column(name = "reset_token", nullable = false, unique = true)
    private String resetToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

}
