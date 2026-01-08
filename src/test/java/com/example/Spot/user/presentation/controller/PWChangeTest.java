package com.example.Spot.user.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.Spot.user.application.security.TokenHashing;
import com.example.Spot.user.application.service.JoinService;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.ResetTokenEntity;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.repository.ResetTokenRepository;
import com.example.Spot.user.domain.repository.UserAuthRepository;
import com.example.Spot.user.infrastructure.repository.FakeEmailSender;
import com.example.Spot.user.presentation.dto.request.JoinDTO;

import jakarta.transaction.Transactional;



@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled("Password reset feature not yet implemented")
class PWChangeTest {

    @Autowired
    private JoinService joinService;
    @Autowired
    private UserAuthRepository userAuthRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FakeEmailSender fakeEmailSender;
    @Autowired
    private TokenHashing tokenHashing;
    @Autowired
    private ResetTokenRepository resetTokenRepository;


    @Test
    @DisplayName("패스워드변경_로그아웃상태_resetToken: 토큰 발급 -> 토큰으로 비밀번호 재설정 -> 토큰 1회성 폐기")
    void 패스워드변경_로그아웃상태_resetToken() throws Exception {
        // given: 회원 가입
        JoinDTO dto = new JoinDTO();
        dto.setUsername("resetUser");
        dto.setPassword("oldPw1234");
        dto.setNickname("spot");
        dto.setRoadAddress("Seoul");
        dto.setAddressDetail("123-45");
        dto.setEmail("reset@spot.com");
        dto.setRole(Role.CUSTOMER);
        dto.setMale(true);
        dto.setAge(24);

        joinService.joinProcess(dto);

        UserAuthEntity authBefore = userAuthRepository.findByUser_Username("resetUser").orElseThrow();
        String oldHashed = authBefore.getHashedPassword();

        // =========================
        // when-1: 로그아웃 상태에서 reset token 발급 요청 (JWT 없이)
        // =========================
        // endpoint: POST /api/auth/password/reset-token
        // body: {"username":"resetUser","email":"reset@spot.com"}
        //
        //
        // - username/email 매칭되면 resetToken 생성 + 저장 + (메일 발송 생략)
        // - 응답으로 resetToken을 "테스트 편의상" 반환하거나 아니면 DB에서 토큰을 조회할 수 있게 한다.
        //
        // 실서비스 - 이메일로 전송하나, test에서 토큰 확보 위해
        //    (1) test profile에서만 token을 응답으로 내려줌
        //    (2) repository로 최근 토큰을 조회
        mockMvc.perform(
                        post("/api/auth/reset-password-request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "email": "reset@spot.com" }
                                        """)
                )
                .andExpect(status().isOk());

        // 파싱
        String mailBody = fakeEmailSender.lastEmail().body();
        String resetToken = extractTokenFromMail(mailBody);
        assertThat(resetToken).isNotBlank();

        String resetTokenHashBefore = tokenHashing.sha256WithPepper(resetToken);
        Optional<ResetTokenEntity> before = resetTokenRepository.findByResetTokenHash(resetTokenHashBefore);
        assertThat(before).isPresent();

        // =========================
        // when-2: reset token으로 새 비밀번호 설정 (JWT 없이)
        // =========================
        // endpoint: POST /api/auth/password/reset
        // body: {"resetToken":"...","newPassword":"newPw5678"}
        //
        // - resetToken 유효성(존재/만료/미사용) 검증
        // - 해당 user의 UserAuth.hashed_password BCrypt로 업데이트
        // - resetToken은 즉시 삭제 처리
        mockMvc.perform(
                        post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "token": "%s",
                          "newPassword": "newPw5678"
                        }
                        """.formatted(resetToken))
                )
                .andExpect(status().isOk());

        // =========================
        // then-1: 비밀번호가 실제로 바뀌었는지 (BCrypt 매칭)
        // =========================
        UserAuthEntity authAfter = userAuthRepository.findByUser_Username("resetUser").orElseThrow();

        assertThat(authAfter.getHashedPassword()).isNotEqualTo(oldHashed);
        assertThat(passwordEncoder.matches("newPw5678", authAfter.getHashedPassword())).isTrue();
        assertThat(passwordEncoder.matches("oldPw1234", authAfter.getHashedPassword())).isFalse();
        // =========================
        // then-2: resetToken의 1회성 test
        // =========================
        // ResetTokenRepository/Entity가 생기면 활성화할 테스트
        //
        Optional<ResetTokenEntity> after = resetTokenRepository.findByResetTokenHash(resetTokenHashBefore);
        assertThat(after).isEmpty();


    }

    /**
     * 아주 단순한 JSON 문자열 추출 헬퍼 (테스트용).
     * 실제로는 ObjectMapper 사용 권장.
     */
    private static String extractTokenFromMail(String body) {
        // 예: "token: 64e19557...."
        String key = "token:";
        int idx = body.indexOf(key);
        if (idx == -1) {
            return "";
        }
        String after = body.substring(idx + key.length()).trim();
        int end = after.indexOf("\n");
        return (end == -1) ? after.trim() : after.substring(0, end).trim();
    }

}
