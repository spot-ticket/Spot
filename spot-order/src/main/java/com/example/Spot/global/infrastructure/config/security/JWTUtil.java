package com.example.Spot.global.infrastructure.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.Spot.global.common.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    // 내부에서 공통으로 쓰는 "서명 검증 + Claims 파싱"
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // (지금 단계) subject를 userId로 쓰고 있으니 그대로 Integer로 반환
    public Integer getUserId(String token) {
        String sub = parseClaims(token).getSubject();
        if (sub == null || sub.isBlank()) {
            return null;
        }
        return Integer.valueOf(sub);
    }

    // (Cognito 전환 대비) subject를 그대로 String으로도 꺼낼 수 있게 제공
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    //  role claim (String) -> Role enum
    public Role getRole(String token) {
        String roleStr = parseClaims(token).get("role", String.class);
        if (roleStr == null || roleStr.isBlank()) {
            return null;
        }
        return Role.valueOf(roleStr);
    }

    // access/refresh 구분
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // 만료 여부 (서명 검증 포함된 parseClaims를 쓰므로 안전)
    public boolean isExpired(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp != null && exp.before(new Date());
    }

    // Access Token
    public String createJwt(Integer userId, Role role, Long expiredMs) {
        return Jwts.builder()
                .subject(userId.toString())       // ✅ 지금 단계: subject=userId
                .claim("role", role.name())
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token
    public String createRefreshToken(Integer userId, long expiredMs) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}
