package com.example.Spot.menu.domain.repository;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat; // 검증을 위한 AssertJ
import static org.assertj.core.api.Assertions.tuple;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository; // StoreRepository 필요

@DataJpaTest
class MenuRepositoryTest {
    @Autowired
    private MenuRepository menuRepository;

    private MenuEntity savedMenu;

    @Autowired
    private StoreRepository storeRepository;    // storeRepository를 사용하기 위해 @Autowired 어노테이션을 이용하여 Bean을 주입함

    private StoreEntity savedStore;

    @BeforeEach
    void setUp() {
        // [Given] 1. StoreEntity 생성 및 저장
        StoreEntity store = StoreEntity.builder()
                .name("원조역삼막국수")
                .address("서울시 강남구")
                .phoneNumber("02-4321-8765")
                .openTime(LocalTime.of(11,0))
                .closeTime(LocalTime.of(21,0))
                .build();

        // Store의 ID가 필요하므로 먼저 저장
        savedStore = storeRepository.save(store);

        MenuEntity menu = MenuEntity.builder()
                .store(savedStore)
                .name("육전막국수")
                .category("한식")
                .price(11000)

                .build();

        savedMenu = menuRepository.save(menu);
    }

    @Test
    @DisplayName("메뉴 정보 수정 테스트")
    void updateMenuTest() {
        // 업데이트 진행
        savedMenu.updateMenu("가라아게덮밥", 11000, "일식");

        menuRepository.flush();

        MenuEntity checkMenu = menuRepository.findById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        assertThat(checkMenu.getName()).isEqualTo("가라아게덮밥");
        assertThat(checkMenu.getPrice()).isEqualTo(11000);
    }

    @Test
    @DisplayName("손님용 - 메뉴 ID로 조회한 경우 테스트")
    void findByIdTest() {
        MenuEntity foundMenu = menuRepository.findActiveMenuById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        assertThat(foundMenu.getName()).isEqualTo("육전막국수");
    }

    @Test
    @DisplayName("손님용 - 메뉴판 조회")
    void findActiveMenuTest() {
        // 가게 메뉴판을 보는 것이므로 '가게 ID'를 넘김
        List<MenuEntity> activeMenus = menuRepository.findAllActiveMenus(savedStore.getId());

        assertThat(activeMenus)
                .extracting("name", "category", "price", "isDeleted", "isHidden")
                // [수정 2] contains 안에 tuple(...) 사용
                .contains(
                        tuple("육전막국수", "한식", 11000, false, false)
                );
    }

    @Test
    @DisplayName("사장님용 조회 - 숨김 메뉴는 보이고, 삭제된 건 안 보임")
    void ownerFindTest() {
        // 숨김 처리된 메뉴
        savedMenu.changeHidden(true);

        // 삭제 처리된 메뉴 (안 보여야 함)
        MenuEntity deletedMenu = MenuEntity.builder()
                .store(savedStore)
                .name("가라아게덮밥")
                .price(10000)
                .category("테스트")
                .build();
        deletedMenu.softDelete();
        menuRepository.save(deletedMenu);

        // [When]
        List<MenuEntity> ownerMenu = menuRepository.findAllByStoreIdAndIsDeletedFalse(savedStore.getId());

        // [Then]
        assertThat(ownerMenu)
                .extracting("name", "isHidden", "isDeleted")
                .containsExactly(tuple("육전막국수", true, false));

    }
}
