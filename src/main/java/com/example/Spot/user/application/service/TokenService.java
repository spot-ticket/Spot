package com.example.Spot.user.application.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.user.application.security.TokenHashing;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.RefreshTokenEntity;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.repository.RefreshTokenRepository;
import com.example.Spot.user.domain.repository.UserAuthRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAuthRepository userAuthRepository;
    private final TokenHashing tokenHashing;

    @Value("${security.refresh-token.expire-days:30}")
    private long refreshExpireDays;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 로그인 성공 시: refresh 발급 + 저장 */
    @Transactional
    public RefreshIssueResult issueRefresh(String username) {
        UserAuthEntity auth = userAuthRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Auth not found"));

        LocalDateTime now = LocalDateTime.now();
        String refreshRaw = generateRefreshToken();

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .auth(auth)
                .refreshTokenHash(tokenHashing.sha256WithPepper(refreshRaw))
                .expiresAt(now.plusDays(refreshExpireDays))
                .build();

        refreshTokenRepository.save(entity);

        return new RefreshIssueResult(usernameOf(auth), roleOf(auth), refreshRaw);
    }

    /** refresh 요청 시: 기존 refresh 검증 → revoke → 새 refresh 발급(rotate) */
    @Transactional
    public RefreshIssueResult rotateRefresh(String oldRefreshRaw) {
        LocalDateTime now = LocalDateTime.now();
        RefreshTokenEntity old = refreshTokenRepository.findByRefreshTokenHash(tokenHashing.sha256WithPepper(oldRefreshRaw))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!old.isActive(now)) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        old.revoke(); // revoke + soft delete

        UserAuthEntity auth = old.getAuth();
        String newRefreshRaw = generateRefreshToken();

        RefreshTokenEntity next = RefreshTokenEntity.builder()
                .auth(auth)
                .refreshTokenHash(tokenHashing.sha256WithPepper(newRefreshRaw))
                .expiresAt(now.plusDays(refreshExpireDays))
                .build();

        refreshTokenRepository.save(next);

        return new RefreshIssueResult(usernameOf(auth), roleOf(auth), newRefreshRaw);
    }

    /** logout 요청 시: refresh revoke */
    @Transactional
    public void revokeRefresh(String refreshRaw) {
        refreshTokenRepository.findByRefreshTokenHash(tokenHashing.sha256WithPepper(refreshRaw))
                .ifPresent(RefreshTokenEntity::revoke);
    }

    private String usernameOf(UserAuthEntity auth) {
        return auth.getUser().getUsername();
    }

    private Role roleOf(UserAuthEntity auth) {
        return auth.getUser().getRole();
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record RefreshIssueResult(String username, Role role, String refreshToken) {}
}
