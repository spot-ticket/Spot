package com.example.Spot.menu.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;



@ExtendWith(MockitoExtension.class)
class MenuOptionServiceTest {

    @Mock
    private MenuOptionRepository menuOptionRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private MenuOptionServiceImpl menuOptionService;

    @Test
    @DisplayName("메뉴 옵션을 생성한다")
    void 메뉴_옵션_생성_테스트() {
        // Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        CustomUserDetails user = createMockUser(Role.OWNER);
        StoreEntity store = createStoreEntity(storeId, user.getUserEntity());
        MenuEntity menu = createMenuEntity(store, "가라아게덮밥", 11000, menuId);

        // Record 생성자 사용 (name, detail, price)
        CreateMenuOptionRequestDto request = new CreateMenuOptionRequestDto("밥 추가", "200g", 2000);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByStoreIdAndId(storeId, menuId)).willReturn(Optional.of(menu));
        given(menuOptionRepository.save(any(MenuOptionEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        // 파라미터 순서: storeId, menuId, userId, role, request
        CreateMenuOptionResponseDto result = menuOptionService.createMenuOption(
                storeId,
                menuId,
                user.getUserEntity().getId(),
                Role.OWNER,
                request
        );

        // Then
        assertThat(result.name()).isEqualTo("밥 추가");

        verify(menuOptionRepository).save(any(MenuOptionEntity.class));
    }

    @Test
    @DisplayName("메뉴 옵션을 성공적으로 수정한다")
    void 메뉴_옵션_수정_테스트() {
        // Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        CustomUserDetails user = createMockUser(Role.OWNER);
        StoreEntity store = createStoreEntity(storeId, user.getUserEntity());
        MenuEntity menu = createMenuEntity(store, "메뉴", 1000, menuId);
        MenuOptionEntity option = createMenuOptionEntity(menu, "기본", "설명", 1000, optionId);

        // Record 생성자 사용 (name, detail, price, isAvailable)
        UpdateMenuOptionRequestDto request = new UpdateMenuOptionRequestDto("수정된옵션", "설명수정", 2000, false);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByStoreIdAndId(storeId, menuId)).willReturn(Optional.of(menu));
        given(menuOptionRepository.findById(optionId)).willReturn(Optional.of(option));

        // When
        // 파라미터 순서: storeId, menuId, optionId, userId, role, request (Create와 일관성 유지 가정)
        MenuOptionAdminResponseDto result = menuOptionService.updateMenuOption(
                storeId,
                menuId,
                optionId,
                user.getUserId(),
                Role.OWNER,
                request
        );

        // Then
        assertThat(result.name()).isEqualTo("수정된옵션");
        assertThat(option.isAvailable()).isFalse(); // Dirty Checking
    }

    @Test
    @DisplayName("메뉴 옵션을 숨김 처리한다")
    void 메뉴_옵션_숨김_테스트() {
        // Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        CustomUserDetails user = createMockUser(Role.OWNER);
        StoreEntity store = createStoreEntity(storeId, user.getUserEntity());
        MenuEntity menu = createMenuEntity(store, "메뉴", 1000, menuId);
        MenuOptionEntity option = createMenuOptionEntity(menu, "기본", "설명", 1000, optionId);

        // Record 생성자 사용
        UpdateMenuOptionHiddenRequestDto request = new UpdateMenuOptionHiddenRequestDto(true);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByStoreIdAndId(storeId, menuId)).willReturn(Optional.of(menu));
        given(menuOptionRepository.findById(optionId)).willReturn(Optional.of(option));

        // When
        menuOptionService.hiddenMenuOption(
                storeId,
                menuId,
                optionId,
                user.getUserId(),
                Role.OWNER,
                request
        );

        // Then
        assertThat(option.isHidden()).isTrue();
    }

    @Test
    @DisplayName("다른 메뉴의 옵션을 숨기려 하면 실패한다")
    void 메뉴_옵션_숨김_실패_부모불일치() {
        // Given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID otherMenuId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();

        CustomUserDetails user = createMockUser(Role.OWNER);
        StoreEntity store = createStoreEntity(storeId, user.getUserEntity());

        MenuEntity otherMenu = createMenuEntity(store, "다른 메뉴", 1000, otherMenuId);
        MenuOptionEntity option = createMenuOptionEntity(otherMenu, "옵션", "설명", 1000, optionId);

        UpdateMenuOptionHiddenRequestDto request = new UpdateMenuOptionHiddenRequestDto(true);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByStoreIdAndId(storeId, menuId)).willReturn(Optional.of(createMenuEntity(store, "요청 메뉴", 100, menuId)));
        given(menuOptionRepository.findById(optionId)).willReturn(Optional.of(option));

        // When & Then
        assertThatThrownBy(() ->
                menuOptionService.hiddenMenuOption(
                        storeId,
                        menuId,
                        optionId,
                        user.getUserId(),
                        Role.OWNER,
                        request
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }


    // --- Helper Methods ---

    private StoreEntity createStoreEntity(UUID storeId, UserEntity owner) {
        StoreEntity store = StoreEntity.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);

        if (owner != null) {
            com.example.Spot.store.domain.entity.StoreUserEntity storeUser =
                    com.example.Spot.store.domain.entity.StoreUserEntity.builder()
                            .store(store)
                            .user(owner)
                            .build();
            store.getUsers().add(storeUser);
        }
        return store;
    }

    private MenuEntity createMenuEntity(StoreEntity store, String name, Integer price, UUID menuId) {
        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name(name)
                .category("한식")
                .price(price)
                .description("테스트")
                .imageUrl("test.jpg")
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
                .role(userRole)
                .build();
        // ID 주입 (필수)
        ReflectionTestUtils.setField(userEntity, "id", 1);

        return new CustomUserDetails(userEntity);
    }
}
