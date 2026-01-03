package com.example.Spot.user.application.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.infra.auth.jwt.JWTUtil;
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

    private static final long ACCESS_EXPIRE_MS = 60 * 60 * 10L;
    private static final long REFRESH_EXPIRE_DAYS = 30L;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAuthRepository userAuthRepository;
    private final TokenHashing tokenHashing;
    private final JWTUtil jwtUtil;

    @Transactional
    public void logout(String refreshTokenRaw) {
        String hash = tokenHashing.sha256WithPepper(refreshTokenRaw);

        refreshTokenRepository.findByRefreshTokenHash(hash)
                .ifPresent(RefreshTokenEntity::revoke);
    }

    @Transactional
    public TokenPair refresh(String refreshTokenRaw) {
        LocalDateTime now = LocalDateTime.now();
        String hash = tokenHashing.sha256WithPepper(refreshTokenRaw);

        RefreshTokenEntity oldToken = refreshTokenRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!oldToken.isActive(now)) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        oldToken.revoke();

        UserAuthEntity auth = oldToken.getAuth();
        String newAccess = jwtUtil.createJwt(usernameOf(auth), roleOf(auth), ACCESS_EXPIRE_MS);

        String newRefreshRaw = generateRefreshToken();
        String newHash = tokenHashing.sha256WithPepper(newRefreshRaw);

        RefreshTokenEntity newEntity = RefreshTokenEntity.builder()
                .auth(auth)
                .refreshTokenHash(newHash)
                .expiresAt(now.plusDays(REFRESH_EXPIRE_DAYS))
                .build();

        refreshTokenRepository.save(newEntity);

        return new TokenPair(newAccess, newRefreshRaw);
    }

    @Transactional
    public TokenPair issueOnLoginSuccess(String username) {
        UserAuthEntity auth = userAuthRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Auth not found"));

        String access = jwtUtil.createJwt(usernameOf(auth), roleOf(auth), ACCESS_EXPIRE_MS);

        LocalDateTime now = LocalDateTime.now();
        String refreshRaw = generateRefreshToken();
        String refreshHash = tokenHashing.sha256WithPepper(refreshRaw);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .auth(auth)
                .refreshTokenHash(refreshHash)
                .expiresAt(now.plusDays(REFRESH_EXPIRE_DAYS))
                .build();

        refreshTokenRepository.save(entity);

        return new TokenPair(access, refreshRaw);
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

    public record TokenPair(String accessToken, String refreshToken) {
    }
}
