package com.example.Spot.user.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.Spot.user.application.service.JoinService;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.repository.UserAuthRepository;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.presentation.dto.request.JoinDTO;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled("Password reset feature not yet implemented")
class PWChangeTest {

    @Autowired private JoinService joinService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserAuthRepository userAuthRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private MockMvc mockMvc;

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
        MvcResult tokenResult = mockMvc.perform(
                        post("/api/auth/password/reset-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "resetUser",
                                          "email": "reset@spot.com"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                // 아래 jsonPath는 “토큰을 응답으로 내려주는 테스트 전용 응답”을 가정
                .andExpect(jsonPath("$.resetToken").exists())
                .andReturn();

        String body = tokenResult.getResponse().getContentAsString();
        // 토큰 파싱
        String resetToken = extractJsonString(body, "resetToken");
        assertThat(resetToken).isNotBlank();

        // =========================
        // when-2: reset token으로 새 비밀번호 설정 (JWT 없이)
        // =========================
        // endpoint: POST /api/auth/password/reset
        // body: {"resetToken":"...","newPassword":"newPw5678"}
        //
        // - resetToken 유효성(존재/만료/미사용) 검증
        // - 해당 user의 UserAuth.hashed_password BCrypt로 업데이트
        // - resetToken은 즉시 만료/삭제/used 처리
        mockMvc.perform(
                        post("/api/auth/password/reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "resetToken": "%s",
                                          "newPassword": "newPw5678"
                                        }
                                        """.formatted(resetToken))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

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
        // Optional<PasswordResetTokenEntity> token = passwordResetTokenRepository.findByToken(resetToken);
        // assertThat(token).isEmpty();

    }

    /**
     * 아주 단순한 JSON 문자열 추출 헬퍼 (테스트용).
     * 실제로는 ObjectMapper 사용 권장.
     */
    private static String extractJsonString(String json, String key) {
        // 예: {"resetToken":"abc"} 형태를 가정
        String needle = "\"" + key + "\":";
        int idx = json.indexOf(needle);
        if (idx == -1) {
                return "";
        }
        int start = json.indexOf("\"", idx + needle.length());
        int end = json.indexOf("\"", start + 1);
        return (start == -1 || end == -1) ? "" : json.substring(start + 1, end);
    }

    @Test
    void 패스워드변경_로그인상태_이메일인증() throws Exception {
        // given-1: 회원 생성
        JoinDTO join = new JoinDTO();
        join.setUsername("Testuser");
        join.setPassword("1234");
        join.setNickname("spot");
        join.setRoadAddress("Seoul");
        join.setAddressDetail("123-45");
        join.setEmail("spot@test.com");
        join.setRole(Role.CUSTOMER);
        join.setMale(true);
        join.setAge(24);
        joinService.joinProcess(join);

        // given-2: 패스워드 변경 전 해시 저장 (추후 검증)
        UserAuthEntity authBefore = userAuthRepository
                .findByUser_Username("Testuser")
                .orElseThrow();

        String oldHashed = authBefore.getHashedPassword();

        // =========================
        // when-1: login 요청 -> JWT 발급
        // =========================
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "Testuser")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andReturn();

        // Authorization 헤더에서 JWT 확인
        String authHeader = loginResult.getResponse().getHeader("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Bearer ");

        String token = authHeader.substring("Bearer ".length());

        // =========================
        // when-2: (로그인 상태) 비밀번호 변경용 이메일 인증 코드 발급
        // =========================
        // endpoint:  POST /api/auth/password/change/email-code
        // 실서비스는 email이나 test에서는 test profile에서 반환
        MvcResult codeResult = mockMvc.perform(
                        post("/api/auth/password/change/email-code")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationCode").exists())
                .andReturn();

        String verificationCodeBody = codeResult.getResponse().getContentAsString();
        String verificationCode = extractJsonString(verificationCodeBody, "verificationCode");
        assertThat(verificationCode).isNotBlank();

        // =========================
        // when-3: (로그인 상태) 이메일 인증 코드 + 현재 PW + 새 PW로 변경 요청
        // =========================
        // endpoint: POST /api/auth/password/change
        mockMvc.perform(
                        post("/api/auth/password/change")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "currentPassword": "1234",
                                      "newPassword": "5678",
                                      "verificationCode": "%s"
                                    }
                                    """.formatted(verificationCode))
                )
                .andExpect(status().isOk());

        // =========================
        // then: UserAuthEntity의 비밀번호가 변경되었는지 검증
        // =========================
        UserAuthEntity authAfter = userAuthRepository
                .findByUser_Username("Testuser")
                .orElseThrow();

        // 해시 값 검증
        assertThat(authAfter.getHashedPassword()).isNotEqualTo(oldHashed);

        // BCrypt 매칭 검증
        assertThat(passwordEncoder.matches("5678", authAfter.getHashedPassword())).isTrue();
        assertThat(passwordEncoder.matches("1234", authAfter.getHashedPassword())).isFalse();

    }

}
