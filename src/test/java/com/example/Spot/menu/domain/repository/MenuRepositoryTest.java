package com.example.Spot.menu.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given; // Mockito 추가

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean; // MockBean 추가
import org.springframework.data.domain.AuditorAware; // AuditorAware 추가

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

@DataJpaTest
class MenuRepositoryTest {

    private static final Integer CREATOR_ID = 107;
    private static final Integer EDITOR_ID = 103;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private StoreRepository storeRepository;

    // [핵심 1] JPA Auditing을 위한 가짜(Mock) 객체 주입
    @MockBean
    private AuditorAware<Integer> auditorAware;

    private MenuEntity savedMenu;
    private StoreEntity savedStore;

    @BeforeEach
    void 가게_메뉴_옵션_생성() {
        // [핵심 2] 저장(save) 시 CREATOR_ID(107)가 들어가도록 설정
        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(CREATOR_ID));

        // 1. StoreEntity 생성
        StoreEntity store = StoreEntity.builder()
                .name("원조역삼막국수")
                .addressDetail("서울시 강남구")
                .roadAddress("서울시 강남구 테헤란로 123")
                .phoneNumber("02-4321-8765")
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        savedStore = storeRepository.save(store);

        // 2. MenuEntity 생성
        MenuEntity menu = MenuEntity.builder()
                .store(savedStore)
                .name("육전물막국수")
                .category("한식")
                .price(11000)
                .description("시원한 동치미와 양지 육수 베이스의 막국수입니다.")
                .imageUrl("test.jpg")
                .build();

        savedMenu = menuRepository.save(menu);
    }

    @Test
    @DisplayName("메뉴 정보를 수정하는 테스트")
    void 메뉴_업데이트_테스트() {

        // 1. When: 업데이트 진행
        savedMenu.updateMenu("가라아게덮밥", 11000, "일식", "매콤한 소스가 들어갔습니다.", "new_img.jpg");

        // 2. DB 반영 (Flush로 강제 업데이트 쿼리 실행)
        menuRepository.flush();

        // 3. 다시 조회 (영속성 컨텍스트 초기화 없이 조회하면 1차 캐시에서 가져오므로 updatedBy 확인 가능)
        MenuEntity checkMenu = menuRepository.findById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // 검증
        assertThat(checkMenu.getName()).isEqualTo("가라아게덮밥");
        assertThat(checkMenu.getPrice()).isEqualTo(11000);
        assertThat(checkMenu.getCategory()).isEqualTo("일식");
    }

    @Test
    @DisplayName("특정 메뉴를 상세 조회합니다.")
    void 메뉴_상세_조회_테스트() {
        // [수정] 표준 JPA 메서드 사용 (findActiveMenuById -> findById)
        // 만약 '삭제안된 것만' 조회하고 싶다면 findByIdAndIsDeletedFalse 사용 필요
        MenuEntity foundMenu = menuRepository.findById(savedMenu.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        assertThat(foundMenu.getName()).isEqualTo("육전물막국수");
    }

    @Test
    @DisplayName("주문 가능한 메뉴 전체를 조회")
    void 주문_가능한_메뉴_조회() {
        // [수정] findAllActiveMenus -> 표준 명명규칙으로 변경
        // "가게 ID가 같고 + 삭제 안됐고 + 숨김 아닌 것"
        List<MenuEntity> activeMenus = menuRepository.findAllActiveMenus(savedStore.getId());

        assertThat(activeMenus)
                .extracting("name", "category", "price", "isDeleted", "isHidden")
                .contains(
                        tuple("육전물막국수", "한식", 11000, false, false)
                );
    }

    @Test
    @DisplayName("[가게] 삭제된 메뉴를 제외한 모든 메뉴를 조회")
    void 삭제_옵션_메뉴_제외_테스트() {
        // [Given] 1. 기존 메뉴 숨김 처리
        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(EDITOR_ID));
        savedMenu.changeHidden(true);
        menuRepository.save(savedMenu); // flush 대신 save 호출로 확실히 반영

        // [Given] 2. 삭제 처리된 메뉴 생성
        MenuEntity deletedMenu = MenuEntity.builder()
                .store(savedStore)
                .name("가라아게덮밥")
                .price(10000)
                .category("테스트")
                .build();
        menuRepository.save(deletedMenu);

        deletedMenu.softDelete(EDITOR_ID); // 삭제 처리
        menuRepository.save(deletedMenu);

        // [When] 삭제되지 않은 메뉴만 조회
        List<MenuEntity> ownerMenu = menuRepository.findAllByStoreIdAndIsDeletedFalse(savedStore.getId());

        // [Then] '육전물막국수'는 숨김(Hidden=true)이지만 삭제(Deleted=false)는 아니므로 조회되어야 함
        assertThat(ownerMenu)
                .hasSize(1)
                .extracting("name", "isHidden", "isDeleted")
                .containsExactly(tuple("육전물막국수", true, false));
    }
}
