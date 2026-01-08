package com.example.Spot.menu.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.Spot.global.TestSupport;

class MenuOptionEntityTest extends TestSupport {

    // 테스를 위한 임의의 유저 ID
    private static final Integer TEST_USER_ID = 107;

    // 테스를 위한 임의의 유저 ID
    private static final Integer TEST_USER_ID = 107;

    @Test
    @DisplayName("메뉴 옵션이 정상적으로 생성되었습니다.")
    void 메뉴_옵션_생성_테스트() {
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
                .createdBy(TEST_USER_ID)
                .build();

        // then
        assertThat(menuOption.getCreatedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menuOption.getMenu()).isEqualTo(menu);
        assertThat(menuOption.getName()).isEqualTo("육전추가");
        assertThat(menuOption.getDetail()).isEqualTo("4조각");
        assertThat(menuOption.getPrice()).isEqualTo(4000);

        System.out.println("====== [옵션 등록 결과] ======");
        System.out.println("옵션명: " + menuOption.getName());
        System.out.println("가격: " + menuOption.getPrice());
        System.out.println("등록자: " + menuOption.getCreatedBy());
        System.out.println("============================\n");
    }

    @Test
    @DisplayName("메뉴 옵션이 정상적으로 변경되었습니다.")
    void 메뉴_옵션_변경_테스트() {
        // 1. given
        MenuOptionEntity menuOption = MenuOptionEntity.builder()
                .name("육전추가")
                .detail("4조각")
                .price(4000)
                .createdBy(103)
                .build();

        System.out.println("====== [수정 전] ======");
        System.out.println("옵션명: " + menuOption.getName());
        System.out.println("가격: " + menuOption.getPrice());

        // 2. when
        menuOption.updateOption("면추가", 2000, "곱빼기", TEST_USER_ID);

        // 3. then
        assertThat(menuOption.getUpdatedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menuOption.getName()).isEqualTo("면추가");
        assertThat(menuOption.getDetail()).isEqualTo("곱빼기");
        assertThat(menuOption.getPrice()).isEqualTo(2000);

        System.out.println("====== [수정 후] ======");
        System.out.println("옵션명: " + menuOption.getName());
        System.out.println("가격: " + menuOption.getPrice());
        System.out.println("수정자: " + menuOption.getUpdatedBy());
        System.out.println("============================\n");
    }

    @Test
    @DisplayName("메뉴 옵션이 품절되었습니다.")
    void 메뉴_옵션_품절_여부_테스트() {
        // given
        MenuOptionEntity menuOption = MenuOptionEntity.builder().build();

        // when
        menuOption.changeAvailable(false, TEST_USER_ID);

        // then
        assertThat(menuOption.getUpdatedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menuOption.getIsAvailable()).isFalse();

        System.out.println("====== [품절 처리 결과] ======");
        System.out.println("품절 여부: " + !menuOption.getIsAvailable()); // false면 품절(true)
        System.out.println("수정자: " + menuOption.getUpdatedBy());
        System.out.println("============================\n");
    }

    @Test
    @DisplayName("메뉴 옵션을 삭제하였습니다.")
    void 메뉴_옵션_삭제_테스트() { // 이름 변경
        // given
        MenuOptionEntity menuOption = MenuOptionEntity.builder().build();

        // when
        menuOption.softDelete(TEST_USER_ID);

        // then
        assertThat(menuOption.getDeletedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menuOption.getIsDeleted()).isTrue();

        System.out.println("====== [삭제 처리 결과] ======");
        System.out.println("삭제 여부: " + menuOption.getIsDeleted());
        System.out.println("삭제자: " + menuOption.getDeletedBy());
        System.out.println("============================\n");
    }
}
