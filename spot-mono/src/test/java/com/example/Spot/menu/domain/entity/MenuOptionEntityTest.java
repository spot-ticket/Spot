package com.example.Spot.menu.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MenuOptionEntityTest {

    @Test
    @DisplayName("메뉴 옵션이 정상적으로 등록되었습니다.")
    void createMenuOptionTest() {
        // 1. given
        MenuEntity menu = MenuEntity.builder()
                .name("육전막국수")
                .build();

        // 2. when
        MenuOptionEntity menuOption = MenuOptionEntity.builder()
                .menu(menu)
                .name("육전추가")
                .detail("4조각")
                .price(4000)
                .build();

        // 3. then
        assertThat(menuOption.getMenu()).isEqualTo(menu);
        assertThat(menuOption.getName()).isEqualTo("육전추가");
        assertThat(menuOption.getDetail()).isEqualTo("4조각");
        assertThat(menuOption.getPrice()).isEqualTo(4000);
    }

    @Test
    @DisplayName("메뉴 옵션이 정상적으로 업데이트되었습니다.")
    void updateMenuOption() {
        // 1. given
        MenuOptionEntity menuOption = MenuOptionEntity.builder()
                .name("육전추가")
                .detail("4조각")
                .price(4000)
                .build();

        // 2. when
        menuOption.updateOption("면추가", 2000, "곱빼기");

        // 3. then
        assertThat(menuOption.getName()).isEqualTo("면추가");
        assertThat(menuOption.getDetail()).isEqualTo("곱빼기");
        assertThat(menuOption.getPrice()).isEqualTo(2000);
    }

    @Test
    @DisplayName("메뉴 옵션이 품절되었습니다.")
    void changeAvailable() {
        // 1. given
        MenuOptionEntity menuOption = MenuOptionEntity.builder().build();

        // 2. when
        menuOption.changeAvailable(false);

        // 3. then
        assertThat(menuOption.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("메뉴 옵션을 삭제하였습니다.")
    void deleteMenuOption() { // 이름 변경
        // 1. given
        MenuOptionEntity menuOption = MenuOptionEntity.builder().build();

        // 2. when
        menuOption.softDelete(0); // 혹은 delete()

        // 3. then
        assertThat(menuOption.getIsDeleted()).isTrue();
    }
}
