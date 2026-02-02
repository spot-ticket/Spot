package com.example.Spot.user.presentation.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;




    /**
     * 가짜 JWT
     * JwtFilter가 최종적으로 만들어야 하는 결과물(= Authentication + principal(userId) + authorities(role))을
     * 테스트에서 SecurityContext에 직접 주입해서 "필터를 통과한 상태"를 흉내내는 것
     * Authorization 헤더/토큰 검증 로직은 실행x
     */
    private UsernamePasswordAuthenticationToken auth(Integer userId, String role) {
        TestJwtPrincipal principal = new TestJwtPrincipal(userId, role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    static class TestJwtPrincipal {
        private final Integer userId;
        private final String role;

        TestJwtPrincipal(Integer userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        public Integer getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }
    }

    @Test
    void 회원조회_본인userId로_성공() throws Exception {
        Integer userId = 503;

        MvcResult result = mockMvc.perform(
                        get("/api/users/{userId}", userId)
                                .with(authentication(auth(userId, "MASTER")))
                )
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("=== GET USER RESPONSE ===");
        System.out.println(result.getResponse().getContentAsString());
    }


    @Test
    void 회원수정_본인userId로_성공() throws Exception {
        Integer userId = 503;

        String requestBody = """
            {
              "nickname": "master-updated",
              "email": "spot****@gmail.com",
              "address": "서울특별시 **구 **동 *****"
            }
            """;

        mockMvc.perform(
                patch("/api/users/{userId}", userId)
                        .with(authentication(auth(userId, "USER")))
                        .contentType("application/json")
                        .content(requestBody)
        ).andExpect(status().isOk());


    }

//    @Test
//    void 회원가입_후_회원삭제_me_성공() throws Exception {
//        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
//        String username = "delete_test_" + suffix;
//
//        String joinRequest = """
//            {
//              "username": "%s",
//              "password": "Test1234!",
//              "email": "delete_%s@test.com",
//              "nickname": "deleteUser",
//              "roadAddress": "서울시 강남구",
//              "addressDetail": "101동",
//              "age": 25,
//              "male": true,
//              "role": "MASTER"
//            }
//            """.formatted(username, suffix);
//
//        mockMvc.perform(
//                        post("/api/join")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(joinRequest)
//                )
//                .andExpect(status().isOk());
//
//        UserEntity user = userRepository.findByUsername(username).orElseThrow();
//        Integer userId = user.getId();
//
//        UserAuthEntity userAuth = userAuthRepository.findByUserId(userId).orElseThrow();
//
//        CustomUserDetails principal = new CustomUserDetails(user, userAuth);
//        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                principal,
//                null,
//                List.of(new SimpleGrantedAuthority("ROLE_MASTER"))
//        );
//
//        mockMvc.perform(
//                        delete("/api/users/me")
//                                .with(authentication(authToken))
//                )
//                .andExpect(status().isOk());
//    }



    @Test
    void 회원검색_MASTER권한_성공() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/users/search")
                                .param("nickname", "스팟")
                                .with(authentication(auth(503, "MASTER")))
                )
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("=== USER SEARCH RESPONSE ===");
        System.out.println(result.getResponse().getContentAsString());
    }
}

