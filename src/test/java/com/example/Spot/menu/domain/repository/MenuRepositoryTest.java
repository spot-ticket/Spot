package com.example.Spot.menu.domain.repository;

import static org.assertj.core.api.Assertions.assertThat; // 검증을 위한 AssertJ
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.Spot.global.TestSupport;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository; // StoreRepository 필요
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class MenuRepositoryTest {

    // 테스를 위한 임의의 유저 ID
    private static final Integer CREATOR_ID = 107;
    private static final Integer EDITOR_ID = 103;

    @Autowired
    private MenuRepository menuRepository;

    private MenuEntity savedMenu;

    @Autowired
    private StoreRepository storeRepository;    // storeRepository를 사용하기 위해 @Autowired 어노테이션을 이용하여 Bean을 주입함

    private StoreEntity savedStore;

    @BeforeEach
    void 가게_메뉴_옵션_생성() {
        // [Given] 1. StoreEntity 생성 및 저장
        StoreEntity store = StoreEntity.builder()
                .name("원조역삼막국수")
                .addressDetail("서울시 강남구")
                .roadAddress("서울시 강남구 테헤란로 123")
                .phoneNumber("02-4321-8765")
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        // Store Entity 수정전이라서 추가한 코드
        ReflectionTestUtils.setField(store, "createdBy", CREATOR_ID);

        // Store의 ID가 필요하므로 먼저 저장
        savedStore = storeRepository.save(store);

        MenuEntity menu = MenuEntity.builder()
                .store(savedStore)
                .name("육전물막국수")
                .category("한식")
                .price(11000)
                .description("시원한 동치미와 양지 육수 베이스의 막국수입니다.")
                .imageUrl("test.jpg")
                .createdBy(CREATOR_ID)
                .build();

        savedMenu = menuRepository.save(menu);
    }

    @Test
    @DisplayName("메뉴 정보를 수정하는 테스트")
    void 메뉴_업데이트_테스트() {
        // 1. When: 업데이트 진행
        savedMenu.updateMenu("가라아게덮밥", 11000, "일식", "매콤한 소스가 들어갔습니다.", "new_img.jpg", EDITOR_ID);

        // 2. DB 반영 (Flush)
        // flush()는 변경 내용을 DB에 쿼리로 날리는 역할
        menuRepository.flush();

        // Then
        MenuEntity checkMenu = menuRepository.findById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // 검증
        assertThat(checkMenu.getUpdatedBy()).isEqualTo(EDITOR_ID);
        assertThat(checkMenu.getName()).isEqualTo("가라아게덮밥");
        assertThat(checkMenu.getPrice()).isEqualTo(11000);
        assertThat(checkMenu.getCategory()).isEqualTo("일식");
        assertThat(checkMenu.getDescription()).isEqualTo("매콤한 소스가 들어갔습니다.");
        assertThat(checkMenu.getImageUrl()).isEqualTo("new_img.jpg");
    }

    @Test
    @DisplayName("특정 메뉴를 상세 조회합니다.")
    void 메뉴_상세_조회_테스트() {
        MenuEntity foundMenu = menuRepository.findActiveMenuById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        assertThat(foundMenu.getName()).isEqualTo("육전물막국수");
    }

    @Test
    @DisplayName("주문 가능한 메뉴 전체를 조회")
    void 주문_가능한_메뉴_조회() {
        // 가게 메뉴판을 보는 것이므로 '가게 ID'를 넘김
        List<MenuEntity> activeMenus = menuRepository.findAllActiveMenus(savedStore.getId());

        assertThat(activeMenus)
                .extracting("name", "category", "price", "isDeleted", "isHidden")
                // contains 안에 tuple(...) 사용
                .contains(
                        tuple("육전물막국수", "한식", 11000, false, false)
                );
    }

    @Test
    @DisplayName("[가게] 삭제된 메뉴를 제외한 모든 메뉴를 조회")
    void 삭제_옵션_메뉴_제외_테스트() {
        // 숨김 처리된 메뉴
        savedMenu.changeHidden(true, CREATOR_ID);
        menuRepository.flush();

        // 삭제 처리된 메뉴 생성(안 보여야 함)
        MenuEntity deletedMenu = MenuEntity.builder()
                .store(savedStore)
                .name("가라아게덮밥")
                .price(10000)
                .category("테스트")
                .createdBy(CREATOR_ID)
                .build();

        menuRepository.save(deletedMenu);
        deletedMenu.softDelete(EDITOR_ID); // 삭제 처리
        menuRepository.save(deletedMenu);

        // [When]
        List<MenuEntity> ownerMenu = menuRepository.findAllByStoreIdAndIsDeletedFalse(savedStore.getId());

        // [Then]
        assertThat(ownerMenu)
                .hasSize(1)
                .extracting("name", "isHidden", "isDeleted")
                .containsExactly(tuple("육전물막국수", true, false));

    }
}
