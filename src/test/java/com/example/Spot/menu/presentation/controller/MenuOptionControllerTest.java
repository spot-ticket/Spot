//package com.example.Spot.menu.presentation.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.willDoNothing;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.util.List;
//import java.util.UUID;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.MediaType;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.example.Spot.infra.auth.security.CustomUserDetails;
//import com.example.Spot.menu.application.service.MenuOptionService;
//import com.example.Spot.menu.domain.entity.MenuEntity;
//import com.example.Spot.menu.domain.entity.MenuOptionEntity;
//import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
//import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
//import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
//import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;
//import com.example.Spot.menu.presentation.dto.response.UpdateMenuOptionResponseDto;
//import com.example.Spot.store.domain.entity.StoreEntity;
//import com.example.Spot.user.domain.entity.UserEntity;
//import com.example.Spot.user.domain.Role;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@WebMvcTest(MenuOptionController.class)
//@AutoConfigureMockMvc
//class MenuOptionControllerTest {
//    @Autowired
//    private MockMvc mockMvc;    // 브라우저 대신 요청을 보내줌
//
//    @Autowired
//    private ObjectMapper objectMapper;  // Java 객체 -> JSON 변환기
//
//    @MockitoBean
//    private MenuOptionService menuOptionService;    // 가짜 서비스
//
//    @Test
//    @DisplayName("[GET] 메뉴 옵션 조회 성공")
//    void 메뉴_옵션_조회_테스트() throws Exception {
//        // given
//        UUID storeId = UUID.randomUUID();
//        UUID menuId = UUID.randomUUID();
//        UUID optionId = UUID.randomUUID();
//
//        CustomUserDetails mockUser = createMockUser(Role.OWNER);
//        StoreEntity store = createStoreEntity(storeId);
//        MenuEntity menu = createMenuEntity(store, menuId);
//        MenuOptionEntity option = createMenuOptionEntity(menu, optionId, "면 추가");
//
//        // DTO 생성
//        List<MenuOptionAdminResponseDto> data = List.of(new MenuOptionAdminResponseDto(option));
//
//        // 가짜 서비스 설정
//        given(menuOptionService.getOptions(Role.OWNER, storeId, menuId)).willReturn(data);
//
//        // when & then (실행 및 검증)
//        mockMvc.perform(get("/api/stores/{storeId}/menus/{menuId}/options", storeId, menuId)
//                        .with(user(mockUser))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print()) // 콘솔에 요청/응답 찍어보기
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("[POST] 메뉴 옵션 생성 테스트 성공")
//    void 메뉴_옵션_생성_테스트() throws Exception {
//        // given
//        UUID storeId = UUID.randomUUID();
//        UUID menuId = UUID.randomUUID();
//        UUID optionId = UUID.randomUUID();
//
//        CreateMenuOptionRequestDto request = new CreateMenuOptionRequestDto();
//        ReflectionTestUtils.setField(request, "name", "면 추가");
//        ReflectionTestUtils.setField(request, "price", 3000);
//        ReflectionTestUtils.setField(request, "detail", "곱빼기");
//
//        CustomUserDetails mockUser = createMockUser(Role.OWNER);
//        StoreEntity store = createStoreEntity(storeId);
//        MenuEntity menu = createMenuEntity(store, menuId);
//        MenuOptionEntity option = createMenuOptionEntity(menu, optionId, request.getName());
//
//        CreateMenuOptionResponseDto response = new CreateMenuOptionResponseDto(option);
//
//        given(menuOptionService.createMenuOption(
//                any(UserEntity.class),
//                eq(storeId),
//                eq(menuId),
//                any(CreateMenuOptionRequestDto.class)
//        )).willReturn(response);
//
//        // when & then
//        mockMvc.perform(post("/api/stores/{storeId}/menus/{menuId}/options", storeId, menuId)
//                        .with(csrf()) // POST 요청 필수
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)) // DTO -> JSON 변환
//                        .with(user(mockUser))) // 인증 정보 주입
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.result.name").value("면 추가")) // 응답 필드 확인
//                .andExpect(jsonPath("$.result.option_id").value(optionId.toString())); // ID 생성 확인
//    }
//
//    @Test
//    @DisplayName("[PATCH] 메뉴 수정 테스트 성공")
//    void 메뉴_수정_테스트() throws Exception{
//        // given
//        UUID storeId = UUID.randomUUID();
//        UUID menuId = UUID.randomUUID();
//        UUID optionId = UUID.randomUUID();
//
//        UpdateMenuOptionRequestDto request = new UpdateMenuOptionRequestDto();
//        ReflectionTestUtils.setField(request, "name", "육전 추가");
//        ReflectionTestUtils.setField(request, "price", 5000);
//        ReflectionTestUtils.setField(request, "detail", "4조각");
//
//        CustomUserDetails mockUser = createMockUser(Role.OWNER);
//        StoreEntity store = createStoreEntity(storeId);
//        MenuEntity menu = createMenuEntity(store, menuId);
//        MenuOptionEntity option = createMenuOptionEntity(menu, optionId, request.getName());
//
//        UpdateMenuOptionResponseDto response = new UpdateMenuOptionResponseDto(option);
//
//        given(menuOptionService.updateMenuOption(
//                any(UserEntity.class),
//                eq(storeId),
//                eq(menuId),
//                eq(optionId),
//                any(UpdateMenuOptionRequestDto.class)
//        )).willReturn(response);
//
//        // when & then
//        mockMvc.perform(patch("/api/stores/{storeId}/menus/{menuId}/options/{optionId}", storeId, menuId, optionId)
//                        .with(csrf()) // POST 요청 필수
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)) // DTO -> JSON 변환
//                        .with(user(mockUser))) // 인증 정보 주입
//                .andDo(print())
//                .andExpect(status().isOk()) // 혹은 .isOk() (Controller 구현에 따라 다름)
//                .andExpect(jsonPath("$.result.name").value("육전 추가")) // 응답 필드 확인
//                .andExpect(jsonPath("$.result.option_id").value(optionId.toString())); // ID 생성 확인
//    }
//
//    @Test
//    @DisplayName("메뉴 옵션 삭제 테스트 성공")
//    void 메뉴_옵션_삭제_테스트() throws Exception {
//        // given
//        UUID storeId = UUID.randomUUID();
//        UUID menuId = UUID.randomUUID();
//        UUID optionId = UUID.randomUUID();
//
//        CustomUserDetails mockUser = createMockUser(Role.OWNER);
//        StoreEntity store = createStoreEntity(storeId);
//        MenuEntity menu = createMenuEntity(store, menuId);
//        MenuOptionEntity option = createMenuOptionEntity(menu, optionId, "면 추가");
//
//        willDoNothing().given(menuOptionService)
//                .deleteMenuOption(any(UserEntity.class), eq(storeId), eq(menuId), eq(optionId));
//
//        mockMvc.perform( // 1. perform 시작
//                        delete("/api/stores/{storeId}/menus/{menuId}/options/{optionId}", storeId, menuId, optionId)
//                                .with(csrf())
//                                .with(user(mockUser))
//                ) // 👈 2. 여기서 perform 괄호를 닫습니다!
//                .andDo(print()) // 3. 그 다음에 andDo를 호출합니다.
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.result").value("해당 옵션이 삭제되었습니다."));
//    }
//
//    // Helper
//    private StoreEntity createStoreEntity(UUID storeId) {
//        StoreEntity store = StoreEntity.builder().build();
//        ReflectionTestUtils.setField(store, "id", storeId);
//        return store;
//    }
//
//    private MenuEntity createMenuEntity(StoreEntity store, UUID menuId) {
//        MenuEntity menu = MenuEntity.builder()
//                .store(store)
//                .name("육전물막국수")
//                .category("한식")
//                .price(13000)
//                .description("테스트")
//                .imageUrl("test.jpg")
//                .build();
//
//        ReflectionTestUtils.setField(menu, "id", menuId);
//
//        return menu;
//    }
//
//    private MenuOptionEntity createMenuOptionEntity(MenuEntity menu, UUID optionId, String name) {
//        MenuOptionEntity option = MenuOptionEntity.builder()
//                .menu(menu)
//                .name(name)
//                .detail("곱빼기")
//                .price(2500)
//                .build();
//
//        ReflectionTestUtils.setField(option, "id", optionId);
//
//        return option;
//    }
//
//    private CustomUserDetails createMockUser(Role userRole) {
//        UserEntity userEntity = UserEntity.builder()
//                .username("test_boss")
//                .nickname("사장님")
//                .email("boss@test.com")
//                .addressDetail("서울시 강남구")
//                .role(userRole)
//                .build();
//
//        ReflectionTestUtils.setField(userEntity, "id", 1);
//
//        return new CustomUserDetails(userEntity);
//    }
//
//    @TestConfiguration
//    @EnableMethodSecurity(prePostEnabled = true)
//    static class TestSecurityConfig {
//        @Bean
//        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//            return http
//                    .csrf(AbstractHttpConfigurer::disable) // 테스트니까 CSRF 끔
//                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // 모든 요청 허용 (인증은 MockMvc가 처리)
//                    .build();
//        }
//    }
//}
