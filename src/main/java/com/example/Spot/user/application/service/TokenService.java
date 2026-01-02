package com.example.Spot.user.application.service;

import com.example.Spot.user.domain.Role;
import com.example.Spot.infra.auth.jwt.JWTUtil; // 너 프로젝트 jwtUtil 패키지에 맞춰 수정
import com.example.Spot.user.application.security.TokenHashing;
import com.example.Spot.user.domain.entity.RefreshTokenEntity;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.repository.RefreshTokenRepository;
import com.example.Spot.user.domain.repository.UserAuthRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAuthRepository userAuthRepository;
    private final TokenHashing tokenHashing;
    private final JWTUtil jwtUtil;

    // 정책값(원하는대로 조정)
    private static final long ACCESS_EXPIRE_MS = 60 * 60 * 10L; // 지금 필터랑 동일(10시간)
    private static final long REFRESH_EXPIRE_DAYS = 30L;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 로그아웃: refresh token revoke (멱등)
     */
    @Transactional
    public void logout(String refreshTokenRaw) {
        String hash = tokenHashing.sha256WithPepper(refreshTokenRaw);

        refreshTokenRepository.findByRefreshTokenHash(hash)
                .ifPresent(RefreshTokenEntity::revoke);
    }

    /**
     * refresh: access 재발급 + refresh rotate (기존 refresh revoke 후 새 refresh 발급/저장)
     */
    @Transactional
    public TokenPair refresh(String refreshTokenRaw) {
        LocalDateTime now = LocalDateTime.now();
        String hash = tokenHashing.sha256WithPepper(refreshTokenRaw);

        RefreshTokenEntity oldToken = refreshTokenRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!oldToken.isActive(now)) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        // rotate: 기존 토큰 revoke
        oldToken.revoke();

        // auth에서 username/role 가져와 access 재발급
        UserAuthEntity auth = oldToken.getAuth();
        String username = auth.getUser().getUsername(); // 너 UserEntity 필드명에 맞춰 수정
        Role role = auth.getUser().getRole();          // 너 UserEntity 필드명에 맞춰 수정

        String newAccess = jwtUtil.createJwt(username, role, ACCESS_EXPIRE_MS);

        // 새 refresh 토큰(랜덤) 발급 + DB 저장(해시)
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

    /**
     * 로그인 성공 직후: refresh 발급/저장 + access 발급
     * (기존 successfulAuthentication에서 access를 만들고 있으니, 여기 결과를 거기로 끼워넣으면 됨)
     */
    @Transactional
    public TokenPair issueOnLoginSuccess(String username) {
        UserAuthEntity auth = userAuthRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Auth not found"));

        Role role = auth.getUser().getRole(); // 너 UserEntity 필드명에 맞춰 수정
        String access = jwtUtil.createJwt(username, role, ACCESS_EXPIRE_MS);

        String refreshRaw = generateRefreshToken();
        String refreshHash = tokenHashing.sha256WithPepper(refreshRaw);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .auth(auth)
                .refreshTokenHash(refreshHash)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_EXPIRE_DAYS))
                .build();

        refreshTokenRepository.save(entity);

        return new TokenPair(access, refreshRaw);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[64]; // 512-bit
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
