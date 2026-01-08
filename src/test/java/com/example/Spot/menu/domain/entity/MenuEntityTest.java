package com.example.Spot.menu.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.Spot.global.TestSupport;
import com.example.Spot.store.domain.entity.StoreEntity;

class MenuEntityTest extends TestSupport {

    // 테스를 위한 임의의 유저 ID
    private static final Integer TEST_USER_ID = 107;

    // 테스를 위한 임의의 유저 ID
    private static final Integer TEST_USER_ID = 107;

    @Test
    @DisplayName("메뉴가 정상적으로 등록되었습니다.")
    void 메뉴_생성_테스트() {
        // 가짜 가게 생성
        StoreEntity mockStore = Mockito.mock(StoreEntity.class);

        // 메뉴 생성
        MenuEntity menu = MenuEntity.builder()
                .store(mockStore)
                .name("육전막국수")
                .category("한식")
                .price(13000)
                .createdBy(TEST_USER_ID)
                .build();

        // 검증
        assertThat(menu.getCreatedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menu.getStore()).isNotNull();
        assertThat(menu.getName()).isEqualTo("육전막국수");
        assertThat(menu.getCategory()).isEqualTo("한식");
        assertThat(menu.getPrice()).isEqualTo(13000);

        System.out.println("====== [메뉴 등록 결과] ======");
        System.out.println("메뉴명: " + menu.getName());
        System.out.println("가격: " + menu.getPrice());
        System.out.println("카테고리: " + menu.getCategory());
        System.out.println("등록자 ID: " + menu.getCreatedBy());
        System.out.println("============================\n");
    }

    @Test
    @DisplayName("메뉴가 정상적으로 업데이트되었습니다.")
    void 메뉴_수정_테스트() {
        // given
        MenuEntity menu = MenuEntity.builder()
                .name("육전막국수")
                .category("한식")
                .price(13000)
                .description("강원도 메밀을 사용한 물 막국수입니다.")
                .imageUrl("old.jpg")
                .createdBy(103)
                .build();

        System.out.println("====== [수정 전] ======");
        System.out.println("메뉴명: " + menu.getName());
        System.out.println("가격: " + menu.getPrice());
        System.out.println("등록자 ID: " + menu.getCreatedBy()); // 눈으로 확인

        // when
        menu.updateMenu("가라아게덮밥", 11000, "일식", "매콤한 소스가 들어갔습니다.", "new_image.jpg", TEST_USER_ID);

        // then
        assertThat(menu.getUpdatedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menu.getName()).isEqualTo("가라아게덮밥");
        assertThat(menu.getPrice()).isEqualTo(11000);
        assertThat(menu.getCategory()).isEqualTo("일식");
        assertThat(menu.getDescription()).isEqualTo("매콤한 소스가 들어갔습니다.");
        assertThat(menu.getImageUrl()).isEqualTo("new_image.jpg");

        System.out.println("====== [수정 후] ======");
        System.out.println("메뉴명: " + menu.getName());
        System.out.println("가격: " + menu.getPrice());
        System.out.println("설명: " + menu.getDescription());
        System.out.println("수정자 ID: " + menu.getUpdatedBy());
        System.out.println("============================\n");
    }

    @Test
    @DisplayName("메뉴가 품절되었습니다.")
    void 메뉴_품절_여부_변경_테스트() {
        MenuEntity menu = MenuEntity.builder()
                .build();

        System.out.println("====== [품절 처리 테스트] ======");
        System.out.println("변경 전 품절여부: " + menu.getIsAvailable());

        menu.changeAvailable(false, TEST_USER_ID);

        assertThat(menu.getUpdatedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menu.getIsAvailable()).isFalse();

        System.out.println("변경 후 품절여부: " + menu.getIsAvailable()); // false가 나와야 함
        System.out.println("수정자 ID: " + menu.getUpdatedBy());
        System.out.println("============================\n");
    }

    @Test
    @DisplayName("메뉴를 숨김 처리하였습니다.")
    void 메뉴_숨김_처리_테스트() {
        MenuEntity menu = MenuEntity.builder().build();

        System.out.println("====== [숨김 처리 테스트] ======");
        System.out.println("변경 전 숨김여부: " + menu.getIsHidden());

        menu.changeHidden(true, TEST_USER_ID);

        assertThat(menu.getUpdatedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menu.getIsHidden()).isTrue();

        System.out.println("변경 후 숨김여부: " + menu.getIsHidden()); // true가 나와야 함
        System.out.println("수정자 ID: " + menu.getUpdatedBy());
        System.out.println("============================\n");
    }

    @Test
    @DisplayName("메뉴를 삭제하였습니다.")
    void deleteMenu() {
        MenuEntity menu = MenuEntity.builder().build();

        System.out.println("====== [삭제 처리 테스트] ======");
        System.out.println("삭제 전 상태: " + menu.getIsDeleted());

        menu.softDelete(TEST_USER_ID);

        assertThat(menu.getDeletedBy()).isEqualTo(TEST_USER_ID);
        assertThat(menu.getIsDeleted()).isTrue();

        System.out.println("삭제 후 상태: " + menu.getIsDeleted()); // true
        System.out.println("삭제자 ID: " + menu.getDeletedBy());
        System.out.println("============================\n");
    }
}
