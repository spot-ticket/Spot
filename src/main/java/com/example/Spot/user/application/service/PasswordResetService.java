package com.example.Spot.user.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.user.domain.entity.ResetTokenEntity;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.ResetTokenRepository;
import com.example.Spot.user.domain.repository.UserAuthRepository;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.infrastructure.repository.EmailSender;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    private static final long TOKEN_TTL_MINUTES = 30;

    @Transactional
    public void sendResetLink(String email) {
        // 1) user 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        // 2) auth 조회 → authId 확보
        UserAuthEntity auth = userAuthRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("인증 정보를 찾을 수 없습니다."));
        UUID authId = auth.getId();

        // 3) 재발급 정책(간단): authId 기준으로 기존 토큰 삭제
        resetTokenRepository.deleteByAuthId(authId);

        // 4) plain token 생성 → hash 저장
        String plainToken = UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256Hex(plainToken);

        ResetTokenEntity row = ResetTokenEntity.issue(authId, tokenHash, TOKEN_TTL_MINUTES);
        resetTokenRepository.save(row);

        // 5) 이메일 전송(프런트 없으니 토큰 안내)
        String body = """
                비밀번호 재설정 토큰입니다. (유효기간 %d분)

                token: %s

                (예) POST /api/auth/reset-password
                { "token": "%s", "newPassword": "새비밀번호" }
                """.formatted(TOKEN_TTL_MINUTES, plainToken, plainToken);

        emailSender.send(email, "비밀번호 재설정", body);
    }

    @Transactional
    public void resetPassword(String plainToken, String newPassword) {
        String tokenHash = sha256Hex(plainToken);

        ResetTokenEntity token = resetTokenRepository.findByResetTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (token.isExpired()) {
            throw new IllegalArgumentException("만료된 토큰입니다.");
        }

        // 1) authId로 auth 조회
        UserAuthEntity auth = userAuthRepository.findById(token.getAuthId())
                .orElseThrow(() -> new IllegalArgumentException("인증 정보를 찾을 수 없습니다."));

        // 2) 비밀번호 변경(auth 테이블에 반영)
        auth.changePassword(passwordEncoder.encode(newPassword));

        // 3) 토큰 1회성 처리(usedAt 없이 삭제)
        resetTokenRepository.delete(token);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("토큰 해시 생성 실패", e);
        }
    }
}
