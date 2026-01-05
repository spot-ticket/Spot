package com.example.Spot.menu.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.repository.MenuRepository;

import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.given;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuServiceImpl menuService;

    @Test
    @DisplayName("[관리자용] 삭제 및 숨김 메뉴를 포함한 모든 메뉴 조회 테스트")
    void getMenusForAdminTest() {
        UUID storeId = UUID.randomUUID();

        // 가짜 Store 생성
        StoreEntity store = StoreEntity.builder()
                .build();
        ReflectionTestUtils.setField(store, "id", storeId); // 강제 주입

        // 가짜 MenuENtity 생성
        MenuEntity menu1 = createMenuEntity(store, "육전물막국수", 13000);
        MenuEntity menu2 = createMenuEntity(store, "가라아게덮밥", 11000);

        given(menuRepository.findAllByStoreId(storeId))
                .willReturn(List.of(menu1, menu2));

        // 2. When (실행)
        List<MenuAdminResponseDto> result = menuService.getMenusForAdmin(storeId, Role.ADMIN);

        // 3. Then (검증)
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getName()).isEqualTo("육전물막국수");

        // 3-3. DTO 변환 시 StoreId가 잘 들어갔는지 확인
        assertThat(result.getFirst().getStoreId()).isEqualTo(storeId);

        // 3-4. Repository가 실제로 호출되었는지 검증
        verify(menuRepository, times(1)).findAllByStoreId(storeId);
    }

    @Test
    @DisplayName("[가게용] 삭제 메뉴를 제외한 모든 메뉴 조회 테스트")
    void getMenusForOwnerTest() {
        UUID storeId = UUID.randomUUID();

        // 가짜 Store 생성
        StoreEntity store = StoreEntity.builder()
                .build();
        ReflectionTestUtils.setField(store, "id", storeId); // 강제 주입

        // 가짜 MenuENtity 생성
        MenuEntity menu1 = createMenuEntity(store, "가라아게덮밥", 11000);

        given(menuRepository.findAllByStoreIdAndIsDeletedFalse(storeId))
                .willReturn(List.of(menu1));

        // 2. When (실행)
        List<MenuAdminResponseDto> result = menuService.getMenusForAdmin(storeId, Role.OWNER);

        // 3. Then (검증)
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("가라아게덮밥");

        verify(menuRepository, times(1)).findAllByStoreIdAndIsDeletedFalse(storeId);
    }

    @Test
    @DisplayName("[손님용] 메뉴 상세 조회 테스트")
    void getMenuDetailTest() {
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        // 가짜 Store 생성
        StoreEntity store = createStoreEntity(storeId);
        MenuEntity menu = createMenuEntity(store, "육전물막국수", 13000);
        ReflectionTestUtils.setField(menu, "id", menuId);
        ReflectionTestUtils.setField(menu, "isHidden", true);

        given(menuRepository.findActiveMenuById(menuId))
                .willReturn(Optional.of(menu));

        MenuPublicResponseDto result = menuService.getMenuDetail(menuId);

        assertThat(result.getName()).isEqualTo("육전물막국수");
        assertThat(result.getPrice()).isEqualTo(13000);
        assertThat(result.getIsHidden()).isTrue();

        verify(menuRepository, times(1)).findActiveMenuById(menuId);
    }

    private StoreEntity createStoreEntity(UUID storeId) {
        StoreEntity store = StoreEntity.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);
        return store;
    }

    private MenuEntity createMenuEntity(StoreEntity store, String name, Integer price) {
        MenuEntity menu = MenuEntity.builder()
                .store(store) // 중요! Store가 있어야 DTO 변환 시 에러 안 남
                .name(name)
                .category("한식")
                .price(price)
                .description("테스트")
                .imageUrl("test.jpg")
                .options(new ArrayList<>())
                .build();

        // ID는 DB 저장 시 생성되므로, 테스트에선 강제로 넣어줘야 함
        ReflectionTestUtils.setField(menu, "id", UUID.randomUUID());

        return menu;
    }
}
