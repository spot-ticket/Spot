package com.example.Spot.user.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;



@SpringBootTest
@AutoConfigureMockMvc
class JoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 회원가입_성공() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        String requestBody = """
            {
              "username": "testuser_%s",
              "password": "Test1234!",
              "email": "test_%s@example.com",
              "nickname": "testnickname",
              "roadAddress": "서울시 강남구",
              "addressDetail": "101동 202호",
              "age": 25,
              "male": true,
              "role": "MASTER"
            }
            """.formatted(suffix, suffix);

        MvcResult result = mockMvc.perform(
                        post("/api/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("=== JOIN RESPONSE ===");
        System.out.println(result.getResponse().getContentAsString());
    }
}

