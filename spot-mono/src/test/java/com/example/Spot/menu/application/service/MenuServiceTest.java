package com.example.Spot.menu.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuOptionRepository menuOptionRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuServiceImpl menuService;

    @Test
    @DisplayName("[관리자용] 삭제 및 숨김 메뉴를 포함한 모든 메뉴 조회 테스트")
    void 관리자용_메뉴_조회_테스트() {
        // 1. Given (준비)
        UUID storeId = UUID.randomUUID();
        UUID menuId1 = UUID.randomUUID(); // 메뉴1 ID 생성
        UUID menuId2 = UUID.randomUUID(); // 메뉴2 ID 생성

        // 가짜 유저 생성
        UserEntity user = UserEntity.builder().build();

        // 가짜 Store 생성
        StoreEntity store = StoreEntity.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);

        // 가짜 MenuEntity 생성 (ID를 직접 주입!)
        MenuEntity menu1 = createMenuEntity(store, "육전물막국수", 13000, menuId1);
        MenuEntity menu2 = createMenuEntity(store, "가라아게덮밥", 11000, menuId2);

        given(menuRepository.findAllByStoreId(storeId))
                .willReturn(List.of(menu1, menu2));

        // 2. When (실행)
        // Service 로직에 맞춰 Role.MASTER 혹은 MANAGER 사용
        List<MenuAdminResponseDto> result = menuService.getMenusForAdmin(storeId, user.getId(), Role.MASTER);

        // 3. Then (검증)
        assertThat(result).hasSize(2);

        // 이름 검증
        assertThat(result.get(0).name()).isEqualTo("육전물막국수");
        assertThat(result.get(1).name()).isEqualTo("가라아게덮밥");

        // ID 검증 (테스트에서 선언한 ID와 일치하는지 확인 가능!)
        assertThat(result.get(0).id()).isEqualTo(menuId1);
        assertThat(result.get(1).id()).isEqualTo(menuId2);

        // StoreId 검증
        assertThat(result.get(0).storeId()).isEqualTo(storeId);

        verify(menuRepository, times(1)).findAllByStoreId(storeId);
    }

    @Test
    @DisplayName("[가게용] 삭제 메뉴를 제외한 모든 메뉴 조회 테스트")
    void 가게용_메뉴_조회_테스트() {
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        // 가짜 유저 생성
        UserEntity user = UserEntity.builder().build();

        // 가짜 Store 생성
        StoreEntity store = StoreEntity.builder()
                .build();
        ReflectionTestUtils.setField(store, "id", storeId); // 강제 주입

        // 가짜 MenuENtity 생성
        MenuEntity menu = createMenuEntity(store, "가라아게덮밥", 11000, menuId);

        given(menuRepository.findAllByStoreIdAndIsDeletedFalse(storeId))
                .willReturn(List.of(menu));

        // 2. When (실행)
        List<MenuAdminResponseDto> result = menuService.getMenusForAdmin(storeId, user.getId(), Role.OWNER);

        // 3. Then (검증)
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("가라아게덮밥");
        assertThat(result.getFirst().id()).isEqualTo(menuId);

        verify(menuRepository, times(1)).findAllByStoreIdAndIsDeletedFalse(storeId);
    }

    @Test
    @DisplayName("[손님용] 메뉴 상세 조회 테스트")
    void 메뉴_상세_조회_테스트() {
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        // 가짜 Store 생성
        StoreEntity store = createStoreEntity(storeId);
        MenuEntity menu = createMenuEntity(store, "육전물막국수", 13000, menuId);
        ReflectionTestUtils.setField(menu, "id", menuId);
        ReflectionTestUtils.setField(menu, "isHidden", true);

        // 메뉴 조회 Mocking
        given(menuRepository.findActiveMenuById(menuId))
                .willReturn(Optional.of(menu));

        // 상세 조회 시 옵션도 같이 가져오므로 빈 리스트라도 리턴해줘야 함
        given(menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId))
                .willReturn(Collections.emptyList());

        MenuResponseDto result = menuService.getMenuDetail(storeId, menuId, 0, Role.CUSTOMER);

        assertThat(result.name()).isEqualTo("육전물막국수");
        assertThat(result.price()).isEqualTo(13000);

        verify(menuRepository, times(1)).findActiveMenuById(menuId);
        verify(menuOptionRepository, times(1)).findAllByMenuIdAndIsDeletedFalse(menuId);
    }

    // Helper
    private StoreEntity createStoreEntity(UUID storeId) {
        StoreEntity store = StoreEntity.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);
        return store;
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

        // ID는 DB 저장 시 생성되므로, 테스트에선 강제로 넣어줘야 함
        ReflectionTestUtils.setField(menu, "id", menuId);

        return menu;
    }
}
