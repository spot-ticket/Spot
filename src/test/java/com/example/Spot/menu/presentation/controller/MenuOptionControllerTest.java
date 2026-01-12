package com.example.Spot.menu.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;


import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.menu.application.service.MenuOptionService;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto; // 추가됨
import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MenuOptionController.class)
@AutoConfigureMockMvc
class MenuOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuOptionService menuOptionService;

    @Test
    @DisplayName("[POST] 메뉴 옵션 생성 테스트 성공")
    void 메뉴_옵션_생성_테스트() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        // createMockUser 내부에서 id=1 설정됨
        CustomUserDetails mockUser = createMockUser(Role.OWNER);

        CreateMenuOptionRequestDto request = new CreateMenuOptionRequestDto("면 추가", "곱빼기", 3000);

        CreateMenuOptionResponseDto response = createMockResponseDto(request.name(), request.price());

        given(menuOptionService.createMenuOption(
                eq(storeId),
                eq(menuId),
                any(Integer.class), // userId
                any(Role.class),    // role
                any(CreateMenuOptionRequestDto.class) // request
        )).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/stores/{storeId}/menus/{menuId}/options", storeId, menuId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("면 추가"))
                .andExpect(jsonPath("$.data.price").value(3000));
    }

    @Test
    @DisplayName("[PATCH] 메뉴 옵션 수정 테스트 성공")
    void 메뉴_옵션_수정_테스트() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        CustomUserDetails mockUser = createMockUser(Role.OWNER);

        UpdateMenuOptionRequestDto request = new UpdateMenuOptionRequestDto("육전 추가", "4조각", 5000, true);

        // 수정 응답은 MenuOptionAdminResponseDto (기존 유지)
        MenuOptionEntity optionEntity = createMenuOptionEntity(storeId, menuId, optionId, request.name());
        MenuOptionAdminResponseDto response = MenuOptionAdminResponseDto.of(optionEntity, Role.OWNER);

        given(menuOptionService.updateMenuOption(
                eq(storeId),
                eq(menuId),
                eq(optionId),
                any(),
                any(),
                any()
        )).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/stores/{storeId}/menus/{menuId}/options/{optionId}", storeId, menuId, optionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("육전 추가"));
    }

    @Test
    @DisplayName("[PATCH] 메뉴 옵션 숨김 테스트 성공")
    void 메뉴_옵션_숨김_테스트() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        CustomUserDetails mockUser = createMockUser(Role.OWNER);

        // Record 생성자 사용
        UpdateMenuOptionHiddenRequestDto request = new UpdateMenuOptionHiddenRequestDto(true);

        willDoNothing().given(menuOptionService).hiddenMenuOption(
                eq(storeId), eq(menuId), eq(optionId),
                any(),
                any(), any()
        );

        // when & then
        mockMvc.perform(patch("/api/stores/{storeId}/menus/{menuId}/options/{optionId}/hide", storeId, menuId, optionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(mockUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("[DELETE] 메뉴 옵션 삭제 테스트 성공")
    void 메뉴_옵션_삭제_테스트() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        CustomUserDetails mockUser = createMockUser(Role.OWNER);

        willDoNothing().given(menuOptionService).deleteMenuOption(
                eq(storeId), eq(menuId), eq(optionId),
                any(Integer.class), any(Role.class)
        );

        // when & then
        mockMvc.perform(delete("/api/stores/{storeId}/menus/{menuId}/options/{optionId}", storeId, menuId, optionId)
                        .with(csrf())
                        .with(user(mockUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    // --- Helpers ---

    private MenuOptionEntity createMenuOptionEntity(UUID storeId, UUID menuId, UUID optionId, String name) {
        StoreEntity store = StoreEntity.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);

        MenuEntity menu = MenuEntity.builder().store(store).build();
        ReflectionTestUtils.setField(menu, "id", menuId);

        MenuOptionEntity option = MenuOptionEntity.builder()
                .menu(menu)
                .name(name)
                .price(1000)
                .build();
        ReflectionTestUtils.setField(option, "id", optionId);
        return option;
    }

    private CreateMenuOptionResponseDto createMockResponseDto(String name, Integer price) {
        return new CreateMenuOptionResponseDto(UUID.randomUUID(), UUID.randomUUID(), name, null);
    }

    private CustomUserDetails createMockUser(Role role) {
        UserEntity user = UserEntity.builder()
                .role(role)
                .build();
        // UserDetails에서 getId()를 호출하므로 ID 세팅 필수
        ReflectionTestUtils.setField(user, "id", 1);
        return new CustomUserDetails(user);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
}
