package com.example.Spot.menu.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.user.domain.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuOptionResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;

@ExtendWith(MockitoExtension.class)
class MenuOptionServiceTest {

    @Mock
    private MenuOptionRepository menuOptionRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuOptionServiceImpl menuOptionService;

    @Test
    @DisplayName("메뉴 옵션 조회 테스트")
    void 메뉴_옵션_조회_테스트() {
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();

        StoreEntity store = createStoreEntity(storeId);
        MenuEntity menu = createMenuEntity(store, "육전물막국수", 13000, menuId);

        MenuOptionEntity option = createMenuOptionEntity(menu, "곱빼기", "면 추가", 1000, optionId);

        given(menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId))
                .willReturn(List.of(option));

        List<MenuOptionResponseDto> result = menuOptionService.getOptions(Role.OWNER, storeId, menuId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("곱빼기");
        assertThat(result.getFirst().getPrice()).isEqualTo(1000);

        verify(menuOptionRepository, times(1)).findAllByMenuIdAndIsDeletedFalse(menuId);
    }

    @Test
    @DisplayName("메뉴 옵션을 생성")
    void 메뉴_옵션_생성_테스트() {
        // Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        // 인증된 유저 정보
        CustomUserDetails user = createMockUser(Role.OWNER);

        // 가게 생성
        StoreEntity store = createStoreEntity(storeId);

        // 유저와 가게 연결
        connectStoreAndUser(store, user.getUserEntity());

        // 메뉴 생성
        MenuEntity menu = createMenuEntity(store, "가라아게덮밥", 11000, menuId);

        // 요청 DTO (생성자나 Builder 방식에 맞춰 수정 필요)
        CreateMenuOptionRequestDto request = new CreateMenuOptionRequestDto();
        ReflectionTestUtils.setField(request, "name", "밥 추가");
        ReflectionTestUtils.setField(request, "price", 2000);
        ReflectionTestUtils.setField(request, "detail", "200g");

        given(menuRepository.findActiveMenuById(menuId)).willReturn(Optional.of(menu));
        // save는 리턴값이 없거나 엔티티를 반환하지만, 여기선 검증만 하면 되므로 Mocking 생략 가능하거나 any() 처리

        // 2. When
        CreateMenuOptionResponseDto result = menuOptionService.createMenuOption(user.getUserEntity(), storeId, menuId, request);

        // 3. Then
        assertThat(result.getName()).isEqualTo("밥 추가");

        verify(menuRepository, times(1)).findActiveMenuById(menuId);
        verify(menuOptionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("[수정] 메뉴 옵션을 성공적으로 수정한다")
    void 메뉴_옵션_수정_테스트() {
        // 1. Given
        UUID storeId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        // 인증된 유저 정보
        CustomUserDetails user = createMockUser(Role.OWNER);

        // 가게 생성
        StoreEntity store = createStoreEntity(UUID.randomUUID());

        // 유저와 가게 연결
        connectStoreAndUser(store, user.getUserEntity());

        // 메뉴 생성
        MenuEntity menu = createMenuEntity(store, "메뉴", 1000, menuId);

        // 메뉴 옵션 생성
        MenuOptionEntity option = createMenuOptionEntity(menu, "테스트옵션", "테스트", 1000, optionId);

        // DTO 생성 및 값 주입
        UpdateMenuOptionRequestDto request = new UpdateMenuOptionRequestDto();
        ReflectionTestUtils.setField(request, "name", "수정된옵션");
        ReflectionTestUtils.setField(request, "price", 2000);
        ReflectionTestUtils.setField(request, "detail", "설명수정");
        ReflectionTestUtils.setField(request, "isAvailable", false);

        given(menuOptionRepository.findById(optionId)).willReturn(Optional.of(option));

        // 2. When
        UpdateMenuOptionResponseDto result = menuOptionService.updateMenuOption(user.getUserEntity(), storeId, optionId, request);

        // 3. Then
        assertThat(result.getName()).isEqualTo("수정된옵션");
        assertThat(option.getIsAvailable()).isFalse();

        verify(menuOptionRepository, times(1)).findById(optionId);
    }

    // Helper 메서드
    private StoreEntity createStoreEntity(UUID storeId) {
        StoreEntity store = StoreEntity.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);

        ReflectionTestUtils.setField(store, "users", new ArrayList<>());
        return store;
    }

    private void connectStoreAndUser(StoreEntity store, UserEntity user) {
        // StoreUserEntity가 가게와 유저의 중간 다리 역할을 합니다.
        com.example.Spot.store.domain.entity.StoreUserEntity storeUser =
                com.example.Spot.store.domain.entity.StoreUserEntity.builder()
                        .store(store)
                        .user(user)
                        .build();

        // validatePermission은 store.getUsers()를 통해 유저의 소속된 가게 확인
        store.getUsers().add(storeUser);
    }

    private MenuEntity createMenuEntity(StoreEntity store, String name, Integer price, UUID menuId) {
        MenuEntity menu = MenuEntity.builder()
                .store(store) // Store가 있어야 DTO 변환 시 에러 안 남
                .name(name)
                .category("한식")
                .price(price)
                .description("테스트")
                .imageUrl("test.jpg")
                .options(new ArrayList<>())
                .build();

        ReflectionTestUtils.setField(menu, "id", menuId);
        return menu;
    }

    private MenuOptionEntity createMenuOptionEntity(MenuEntity menu, String name, String detail, Integer price, UUID optionId) {
        MenuOptionEntity option = MenuOptionEntity.builder()
                .menu(menu)
                .name(name)
                .detail(detail)
                .price(price)
                .build();

        ReflectionTestUtils.setField(option, "id", optionId);
        return option;
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
}
