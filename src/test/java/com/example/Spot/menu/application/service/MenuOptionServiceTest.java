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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

        List<MenuOptionResponseDto> result = menuOptionService.getOptions(menuId, Role.OWNER);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("곱빼기");
        assertThat(result.getFirst().getPrice()).isEqualTo(1000);

        verify(menuOptionRepository, times(1)).findAllByMenuIdAndIsDeletedFalse(menuId);
    }

    @Test
    @DisplayName("메뉴 옵션을 생성")
    void 메뉴_옵션_생성_테스트() {
        // 1. Given
        UUID menuId = UUID.randomUUID();
        StoreEntity store = createStoreEntity(UUID.randomUUID());
        MenuEntity menu = createMenuEntity(store, "가라아게덮밥", 11000, menuId);

        // 요청 DTO (생성자나 Builder 방식에 맞춰 수정 필요)
        CreateMenuOptionRequestDto request = new CreateMenuOptionRequestDto();
        ReflectionTestUtils.setField(request, "name", "밥 추가");
        ReflectionTestUtils.setField(request, "price", 2000);
        ReflectionTestUtils.setField(request, "detail", "200g");

        given(menuRepository.findActiveMenuById(menuId)).willReturn(Optional.of(menu));
        // save는 리턴값이 없거나 엔티티를 반환하지만, 여기선 검증만 하면 되므로 Mocking 생략 가능하거나 any() 처리

        // 2. When
        CreateMenuOptionResponseDto result = menuOptionService.createMenuOption(menuId, request);

        // 3. Then
        assertThat(result.getName()).isEqualTo("밥 추가");

        verify(menuRepository, times(1)).findActiveMenuById(menuId);
        verify(menuOptionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("[수정] 메뉴 옵션을 성공적으로 수정한다")
    void 메뉴_옵션_수정_테스트() {
        // 1. Given
        UUID optionId = UUID.randomUUID();
        MenuEntity menu = createMenuEntity(createStoreEntity(UUID.randomUUID()), "메뉴", 1000, UUID.randomUUID());

        MenuOptionEntity option = createMenuOptionEntity(menu, "기존옵션", "설명", 1000, optionId);

        // ★ 수정: DTO 생성 및 값 주입
        UpdateMenuOptionRequestDto request = new UpdateMenuOptionRequestDto();
        ReflectionTestUtils.setField(request, "name", "수정된옵션");
        ReflectionTestUtils.setField(request, "price", 2000);
        ReflectionTestUtils.setField(request, "detail", "설명수정");
        ReflectionTestUtils.setField(request, "isAvailable", false);

        given(menuOptionRepository.findById(optionId)).willReturn(Optional.of(option));

        // 2. When
        UpdateMenuOptionResponseDto result = menuOptionService.updateMenuOption(optionId, request);

        // 3. Then
        assertThat(result.getName()).isEqualTo("수정된옵션");
        assertThat(option.getIsAvailable()).isFalse();

        verify(menuOptionRepository, times(1)).findById(optionId);
    }

    // Helper 메서드
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
}
