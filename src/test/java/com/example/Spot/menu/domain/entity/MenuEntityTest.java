package com.example.Spot.menu.domain.entity;

import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.global.common.UpdateBaseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.assertThat;

class MenuEntityTest {

    @Test
    @DisplayName("메뉴가 정상적으로 등록되었습니다.")
    void createMenuTest() {
        // 1. 가짜 가게 생성
        StoreEntity mockStore = Mockito.mock(StoreEntity.class);

        // 2. 메뉴 생성
        MenuEntity menu = MenuEntity.builder()
                .store(mockStore)
                .name("육전막국수")
                .category("한식")
                .price(13000)
                .build();

        // 3. 확인
        assertThat(menu.getStore()).isNotNull();
        assertThat(menu.getName()).isEqualTo("육전막국수");
        assertThat(menu.getCategory()).isEqualTo("한식");
        assertThat(menu.getPrice()).isEqualTo(13000);
    }

    @Test
    @DisplayName("메뉴가 정상적으로 업데이트되었습니다.")
    void updateMenu() {
        // 1. given
        MenuEntity menu = MenuEntity.builder()
                .name("육전막국수")
                .category("한식")
                .price(13000)
                .build();

        // 2. when
        menu.updateMenu("가라아게덮밥", 11000, "일식");

        assertThat(menu.getName()).isEqualTo("가라아게덮밥");
        assertThat(menu.getPrice()).isEqualTo(11000);
        assertThat(menu.getCategory()).isEqualTo("일식");
    }

    @Test
    @DisplayName("메뉴가 품절되었습니다.")
    void changeAvailable() {
        MenuEntity menu = MenuEntity.builder()
                .build();

        menu.changeAvailable(false);

        assertThat(menu.getIsAvailable()).isFalse();
    }

    @Test
    @DisplayName("메뉴를 숨김 처리하였습니다.")
    void changeHidden() {
        MenuEntity menu = MenuEntity.builder().build();

        menu.changeHidden(true);

        assertThat(menu.getIsHidden()).isTrue();
    }

    @Test
    @DisplayName("메뉴를 삭제하였습니다.")
    void deleteMenu() {
        MenuEntity menu = MenuEntity.builder().build();

        menu.softDelete();

        assertThat(menu.getIsDeleted()).isTrue();
    }
}