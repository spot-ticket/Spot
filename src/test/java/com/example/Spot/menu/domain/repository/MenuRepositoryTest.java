package com.example.Spot.menu.domain.repository;

import static org.assertj.core.api.Assertions.assertThat; // 검증을 위한 AssertJ
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository; // StoreRepository 필요import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class MenuRepositoryTest {
    @Autowired
    private MenuRepository menuRepository;

    private MenuEntity savedMenu;

    @Autowired
    private StoreRepository storeRepository;    // storeRepository를 사용하기 위해 @Autowired 어노테이션을 이용하여 Bean을 주입함

    private StoreEntity savedStore;

    @BeforeEach
    void 가게_메뉴_메뉴_옵션_생성() {
        // [Given] 1. StoreEntity 생성 및 저장
        StoreEntity store = StoreEntity.builder()
                .name("원조역삼막국수")
                .addressDetail("서울시 강남구")
                .roadAddress("서울시 강남구 테헤란로 123") // 👈 이 줄을 추가하세요!
                .phoneNumber("02-4321-8765")
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        ReflectionTestUtils.setField(store, "createdBy", 123); // ID 타입이 Long이라면 123L
        ReflectionTestUtils.setField(store, "createdAt", LocalDateTime.now());

        // Store의 ID가 필요하므로 먼저 저장
        savedStore = storeRepository.save(store);

        MenuEntity menu = MenuEntity.builder()
                .store(savedStore)
                .name("육전막국수")
                .category("한식")
                .price(11000)
                .description("")
                .imageUrl("")
                .build();

        ReflectionTestUtils.setField(menu, "createdBy", 123); // ID 타입이 Long이라면 123L
        ReflectionTestUtils.setField(menu, "createdAt", LocalDateTime.now());

        savedMenu = menuRepository.save(menu);
    }

    @Test
    @DisplayName("메뉴 정보를 수정하는 테스트")
    void 메뉴_업데이트_테스트() {
        // 1. When: 업데이트 진행
        savedMenu.updateMenu("가라아게덮밥", 11000, "일식", "매콤한 소스가 들어갔습니다.", "new_img.jpg");

        // 2. DB 반영 (Flush)
        // flush()는 변경 내용을 DB에 쿼리로 날리는 역할
        menuRepository.flush();

        // 3. Then: 다시 조회해서 확인
        MenuEntity checkMenu = menuRepository.findById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // 4. 검증: 변경한 5가지 값이 모두 맞는지 확인!
        assertThat(checkMenu.getName()).isEqualTo("가라아게덮밥");
        assertThat(checkMenu.getPrice()).isEqualTo(11000);
        assertThat(checkMenu.getCategory()).isEqualTo("일식");
        assertThat(checkMenu.getDescription()).isEqualTo("매콤한 소스가 들어갔습니다.");
        assertThat(checkMenu.getImageUrl()).isEqualTo("new_img.jpg");
    }

    @Test
    @DisplayName("[손님] 메뉴 상세 조회")
    void 메뉴_상세_조회_테스트() {
        MenuEntity foundMenu = menuRepository.findActiveMenuById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        assertThat(foundMenu.getName()).isEqualTo("육전막국수");
    }

    @Test
    @DisplayName("[손님] 메뉴 조회")
    void 주문_가능한_메뉴_조회() {
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
    @DisplayName("[가게] 삭제된 메뉴를 제외한 모든 메뉴를 조회")
    void 삭제_옵션_메뉴_제외_테스트() {
        // 숨김 처리된 메뉴
        savedMenu.changeHidden(true);

        // 삭제 처리된 메뉴 (안 보여야 함)
        MenuEntity deletedMenu = MenuEntity.builder()
                .store(savedStore)
                .name("가라아게덮밥")
                .price(10000)
                .category("테스트")
                .build();
        deletedMenu.softDelete(0);

        ReflectionTestUtils.setField(deletedMenu, "createdBy", 123);
        ReflectionTestUtils.setField(deletedMenu, "createdAt", LocalDateTime.now());

        menuRepository.save(deletedMenu);

        // [When]
        List<MenuEntity> ownerMenu = menuRepository.findAllByStoreIdAndIsDeletedFalse(savedStore.getId());

        // [Then]
        assertThat(ownerMenu)
                .extracting("name", "isHidden", "isDeleted")
                .containsExactly(tuple("육전막국수", true, false));

    }
}
