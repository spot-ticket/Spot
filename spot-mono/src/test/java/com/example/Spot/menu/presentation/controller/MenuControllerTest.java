
package com.example.Spot.menu.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.menu.application.service.MenuService;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MenuController.class)
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;    // 브라우저 대신 요청을 보내줌

    @Autowired
    private ObjectMapper objectMapper;  // Java 객체 -> JSON 변환기

    @MockitoBean
    private MenuService menuService;    // 가짜 서비스

    @Test
    @DisplayName("[GET] 메뉴 조회 성공")
    @WithMockUser // 로그인 된 상태라고 가정
    void 메뉴_상세_조회_테스트() throws Exception {
        // given
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        StoreEntity store = createStoreEntity(storeId);
        MenuEntity menu = createMenuEntity(store, "육전물막국수", menuId);

        // DTO 생성
        MenuPublicResponseDto responseDto = MenuPublicResponseDto.of(menu, new ArrayList<>());

        // 가짜 서비스 설정
        given(menuService.getMenuDetail(storeId, menuId, 0, Role.OWNER)).willReturn(responseDto);

        // when & then (실행 및 검증)
        mockMvc.perform(get("/api/stores/{storeId}/menus/{menuId}", storeId, menuId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // 콘솔에 요청/응답 찍어보기
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[POST] 메뉴 생성 테스트 성공")
    void 메뉴_생성_테스트() throws Exception {
        // 1. Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        CreateMenuRequestDto request = new CreateMenuRequestDto("육전물막국수", "한식", 123, null, null);

        // 인증된 유저
        CustomUserDetails customUser = createMockUser(Role.MASTER);

        StoreEntity store = createStoreEntity(storeId);
        MenuEntity menu = createMenuEntity(store, request.name(), menuId);

        given(menuService.createMenu(eq(storeId), any(CreateMenuRequestDto.class), any(), Role.OWNER))
                .willReturn(new CreateMenuResponseDto(menu));

        // 2. When & Then
        mockMvc.perform(post("/api/stores/{storeId}/menus", storeId)
                        .with(csrf())
                        .with(user(customUser)) // 👈 [핵심] 여기서 주입한 customUser가 컨트롤러로 전달됩니다.
                       .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[PATCH] 메뉴 변경 테스트 성공.")
    void 메뉴_변경_테스트() throws Exception {
        // 1. Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        UpdateMenuRequestDto request = new UpdateMenuRequestDto("육전비빔막국수", "한식", 123, "테스트", null, null);

        // ustomUserDetails 객체 생성
        CustomUserDetails customUser = createMockUser(Role.OWNER);

        MenuEntity menu = createMenuEntity(createStoreEntity(storeId), request.name(), menuId);

        given(menuService.updateMenu(eq(storeId), eq(menuId), any(UpdateMenuRequestDto.class), any(), Role.OWNER))
                .willReturn(MenuAdminResponseDto.of(
                        menu,           // 1. MenuEntity
                        List.of(),      // 2. List<MenuOptionEntity> (테스트용 빈 리스트)
                        Role.OWNER      // 3. Role (UserRole)
                ));

        // When & Then
        mockMvc.perform(patch("/api/stores/{storeId}/menus/{menuId}", storeId, menuId)
                        .with(csrf())
                        .with(user(customUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("가라아게덮밥"));
    }

    @Test
    @DisplayName("[PATCH] 메뉴 숨김 테스트 성공")
    void 메뉴_숨김_테스트() throws Exception {
        // 1. Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        UpdateMenuHiddenRequestDto request = new UpdateMenuHiddenRequestDto(true);

        CustomUserDetails userRole = createMockUser(Role.MANAGER);
        //MenuEntity menu = createMenuEntity(createStoreEntity(storeId), "육전물막국수", menuId);

        // void 메서드
        willDoNothing().given(menuService)
                .hiddenMenu(eq(menuId), any(UpdateMenuHiddenRequestDto.class), any(), Role.MANAGER);

        mockMvc.perform(patch("/api/stores/{storeId}/menus/{menuId}/hide", storeId, menuId)
                        .with(csrf())
                        .with(user(userRole))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // perform 닫기
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("해당 메뉴를 숨김 처리하였습니다."));
    }

    // Helper
    private StoreEntity createStoreEntity(UUID storeId) {
        StoreEntity store = StoreEntity.builder()
                                       .name("테스트 스토어")
                                       .roadAddress("여기는 로드 넘버 원")
                                       .addressDetail("123-45")
                                       .phoneNumber("02-1234-5678")
                                       .openTime(LocalTime.of(9, 0))
                                       .closeTime(LocalTime.of(22, 0))
                                       .build();

        ReflectionTestUtils.setField(store, "id", storeId);
        return store;
    }

    private MenuEntity createMenuEntity(StoreEntity store, String name, UUID menuId) {
        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name(name)
                .category("한식")
                .price(13000)
                .description("테스트")
                .imageUrl("test.jpg")
                .build();

        ReflectionTestUtils.setField(menu, "id", menuId);

        return menu;
    }

    private CustomUserDetails createMockUser(Role userRole) {
        UserEntity userEntity = UserEntity.builder()
                .username("test_boss")
                .nickname("사장님")
                .email("boss@test.com")
                .addressDetail("서울시 강남구")
                .role(userRole)
                .build();

        ReflectionTestUtils.setField(userEntity, "id", 1);

        return new CustomUserDetails(userEntity);
    }

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable) // 테스트니까 CSRF 끔
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // 모든 요청 허용 (인증은 MockMvc가 처리)
                    .build();
        }
    }
}
