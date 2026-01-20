package com.example.Spot.user.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
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
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserAuthRepository;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.presentation.dto.request.JoinDTO;

import jakarta.transaction.Transactional;



@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled("Service implementation not yet completed")
class UserServiceTest {

    @Autowired
    private JoinService joinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 회원가입_성공() {
        // given
        JoinDTO dto = new JoinDTO();
        dto.setUsername("Testuser");
        dto.setPassword("1234");
        dto.setNickname("spot");
        dto.setRoadAddress("Seoul");
        dto.setAddressDetail("123-45");
        dto.setEmail("spot@test.com");
        dto.setRole(Role.CUSTOMER);
        dto.setMale(true);
        dto.setAge(24);

        // when
        joinService.joinProcess(dto);

        // then
        // 1. UserEntity 저장 확인
        UserEntity user = userRepository.findByUsername("Testuser")
                .orElseThrow();

        assertThat(user.getNickname()).isEqualTo("spot");
        assertThat(user.getIsDeleted()).isFalse();

        // 2. UserAuthEntity 저장 확인
        UserAuthEntity auth = userAuthRepository
                .findByUser_Username(user.getUsername())
                .orElseThrow();

        // 평문 != 암호문
        assertThat(auth.getHashedPassword()).isNotEqualTo("1234");

        // BCrypt 매칭 검증
        assertThat(passwordEncoder.matches("1234", auth.getHashedPassword()))
                .isTrue();
    }

    @Test
    void 로그인_성공시_Jwt발급되고_bearer로인증() throws Exception {
        // given
        // joinprocess
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

        // when : login 요청
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "Testuser")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andReturn();

        // then: JW//T authorization: bearer로 보내면 인증 붙는지 확인
        // 1. authorization 헤더에서 jwt 확인
        String authHeader = loginResult.getResponse().getHeader("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Bearer ");

        String token = authHeader.substring("Bearer ".length());

        // 2. JWT authorization (bearer보내면 인증 붙는지 확인)
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/test-auth")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());

    }

    @Test
    void 회원조회_성공_jwt사용() throws Exception {
        // given: 회원가입
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

        // when 1: 로그인 -> JWT 발급
        MvcResult loginResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "Testuser")
                                .param("password", "1234")
                )
                .andExpect(status().isOk())   // 302/204면 여기 바꿔줘
                .andReturn();

        String authHeader = loginResult.getResponse().getHeader("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Bearer ");

        String token = authHeader.substring("Bearer ".length());

        // when 2 + then: /users/me 조회
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/{userId}", "id")

                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                // 응답 필드 검증 (JSON 필드명은 실제 DTO에 맞춰 조정)
                .andExpect(jsonPath("$.username").value("Testuser"))
                .andExpect(jsonPath("$.nickname").value("spot"))
                .andExpect(jsonPath("$.sex").value(true))
                .andExpect(jsonPath("$.email").value("spot@test.com"))
                .andExpect(jsonPath("$.address").value("Seoul"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                // createdAt 필드명(created_at vs createdAt)은 네 DTO에 맞춰 택1
                .andExpect(jsonPath("$.createdAt").exists());

    }

    /* 회원 조회 test */
    @Test
    void 회원정보_수정_이메일_주소_닉네임() throws Exception {
        // given: 회원가입
        JoinDTO join = new JoinDTO();
        join.setUsername("testuser");
        join.setPassword("1234");
        join.setNickname("oldNick");
        join.setEmail("old@test.com");
        join.setRoadAddress("OldRoadAddress");
        join.setAddressDetail("OldAddressDetail");
        join.setRole(Role.CUSTOMER);
        join.setMale(true);
        join.setAge(20);

        joinService.joinProcess(join);

        UserEntity user = userRepository.findByUsername("testuser")
                .orElseThrow();

        // 로그인 → JWT
        MvcResult loginResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "testuser")
                                .param("password", "1234")
                )
                .andReturn();

        String token = loginResult.getResponse()
                .getHeader("Authorization")
                .substring("Bearer ".length());

        // when: 회원정보 수정
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/users/{userId}", user.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "new@test.com",
                                      "address": "NewAddress",
                                      "nickname": "newNick"
                                    }
                                """)
                )
                .andExpect(status().isOk());

        // then: DB 반영 확인

        UserEntity updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
        assertThat(updated.getRoadAddress()).isEqualTo("NewRoadAddress");
        assertThat(updated.getRoadAddress()).isEqualTo("NewAddressDetail");
        assertThat(updated.getNickname()).isEqualTo("newNick");
    }

    @Test
    void 로그아웃_refreshtoken_사용() throws Exception {
        // given: 회원가입
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

        // given: 로그인 -> JWT 발급(Authorization 헤더)
        MvcResult loginResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "Testuser")
                                .param("password", "1234")
                )
                .andExpect(status().isOk()) // 302/204면 여기 바꿔줘
                .andReturn();

        String authHeader = loginResult.getResponse().getHeader("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Bearer ");
        String token = authHeader.substring("Bearer ".length());

        // when: 로그아웃
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/logout")
                        .header("Authorization", "Bearer " + token)
        ).andExpect(status().is2xxSuccessful());

        // then: 같은 토큰으로 보호 API 접근 -> 401/403 기대
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/test-auth")
                        .header("Authorization", "Bearer " + token)
        ).andExpect(status().is4xxClientError());

    }

    // 회원탈퇴 test 1
    @Test
    void 회원탈퇴_jwt_인증_본인만_가능() throws Exception {
        // given -1 회원가입
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

        UserEntity user = userRepository.findByUsername("Testuser")
                .orElseThrow();

        Integer userId = user.getId(); // 아직 구현 x

        // given -2 로그인 → JWT 발급
        MvcResult loginResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "Testuser")
                                .param("password", "1234")
                )
                .andExpect(status().isOk())   // 302/204면 수정
                .andReturn();

        String token = loginResult.getResponse()
                .getHeader("Authorization")
                .substring("Bearer ".length());

        // when: 회원 탈퇴 요청
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/users/me", userId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNoContent()); // 또는 isOk()

        // then -1 User soft delete 확인
        UserEntity deletedUser = userRepository.findById(userId)
                .orElseThrow();

        assertThat(deletedUser.getIsDeleted()).isTrue();

        // then -2 UserAuth soft delete 확인 (있다면)
        UserAuthEntity auth = userAuthRepository
                .findByUser_Username("Testuser")
                .orElseThrow();

        assertThat(auth.getIsDeleted()).isTrue();

    }

    // 회원탈퇴 test 2
    @Test
    void 회원탈퇴_실패_JWT없음() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/users/me", 1)
                )
                .andExpect(status().is4xxClientError()); // 401 / 403
    }

    // 회원탈퇴 test 3
    @Test
    void 회원탈퇴_실패_다른유저삭제시도() throws Exception {
        // user1
        JoinDTO u1 = new JoinDTO();
        u1.setUsername("user1");
        u1.setPassword("1234");
        u1.setNickname("u1");
        u1.setRoadAddress("Seoul");
        u1.setAddressDetail("123-45");
        u1.setEmail("u1@test.com");
        u1.setRole(Role.CUSTOMER);
        u1.setMale(true);
        u1.setAge(20);
        joinService.joinProcess(u1);

        // user2
        JoinDTO u2 = new JoinDTO();
        u2.setUsername("user2");
        u2.setPassword("1234");
        u2.setNickname("u2");
        u2.setRoadAddress("Busan");
        u2.setAddressDetail("123-45");
        u2.setEmail("u2@test.com");
        u2.setRole(Role.CUSTOMER);
        u2.setMale(false);
        u2.setAge(25);
        joinService.joinProcess(u2);
        UserEntity user2 = userRepository.findByUsername("user2").orElseThrow();

        // user1 로그인
        MvcResult loginResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "user1")
                                .param("password", "1234")
                )
                .andReturn();

        String token = loginResult.getResponse()
                .getHeader("Authorization")
                .substring("Bearer ".length());

        // user1이 user2 삭제 시도 → 실패
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/users/me", user2.getId())
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden()); // 또는 isNotFound()
    }
}
