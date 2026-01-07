package com.example.Spot.menu.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

@DataJpaTest
class MenuOptionRepositoryTest {

    // [1] ID 분리: 만든 사람(107-사장) vs 수정한 사람(103-직원)
    private static final Integer CREATOR_ID = 107;
    private static final Integer EDITOR_ID = 103;

    @Autowired
    private StoreRepository storeRepository;
    private StoreEntity savedStore;

    @Autowired
    private MenuRepository menuRepository;
    private MenuEntity savedMenu;

    @Autowired
    private MenuOptionRepository menuOptionRepository;
    private MenuOptionEntity savedOption;    // 정상
    private MenuOptionEntity soldOutOption;  // 품절

    @BeforeEach
    void 가게_메뉴_옵션_생성() {
        // 1. 가게 생성 (만든 사람: CREATOR_ID)
        StoreEntity store = StoreEntity.builder()
                .name("원조역삼막국수")
                .addressDetail("서울시 강남구")
                .roadAddress("서울시 강남구 테헤란로 123")
                .phoneNumber("02-4321-8765")
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        // 팀원 코드 우회: 가게 주인(Creator) 설정
        ReflectionTestUtils.setField(store, "createdBy", CREATOR_ID);
        savedStore = storeRepository.save(store);

        // 2. 메뉴 생성 (만든 사람: CREATOR_ID)
        MenuEntity menu = MenuEntity.builder()
                .store(savedStore)
                .name("육전막국수")
                .category("한식")
                .price(11000)
                .createdBy(CREATOR_ID)
                .build();
        savedMenu = menuRepository.save(menu);

        // 3. 정상 옵션 생성 (만든 사람: CREATOR_ID)
        MenuOptionEntity option = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("육전 추가")
                .detail("4조각")
                .price(4000)
                .createdBy(CREATOR_ID)
                .build();
        savedOption = menuOptionRepository.save(option);

        // 4. 품절 옵션 생성 (만든 사람: 107 -> 수정: 103)
        MenuOptionEntity soldOption = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("면 추가")
                .detail("곱빼기")
                .price(2500)
                .createdBy(CREATOR_ID)
                .build();

        menuOptionRepository.save(soldOption);

        // 품절 처리 (직원이 수행)
        soldOption.changeAvailable(false, EDITOR_ID);
        soldOutOption = menuOptionRepository.save(soldOption);
    }

    @Test
    @DisplayName("[검증] 수정자가 등록자와 다르게 잘 저장되는지 확인")
    void 옵션_수정자_확인_테스트() {
        System.out.println("\n========== [테스트 1] 수정자 ID 확인 ==========");
        System.out.println("옵션명: " + soldOutOption.getName());
        System.out.println("등록자 ID (Expected: 107): " + soldOutOption.getCreatedBy());
        System.out.println("수정자 ID (Expected: 103): " + soldOutOption.getUpdatedBy());

        // 검증
        assertThat(soldOutOption.getCreatedBy()).isEqualTo(CREATOR_ID);
        assertThat(soldOutOption.getUpdatedBy()).isEqualTo(EDITOR_ID);
    }

    @Test
    @DisplayName("[손님/가게] 삭제된 옵션을 제외한 모든 옵션 조회 테스트")
    void 옵션_제외_조회() {
        // 1. 삭제할 옵션 생성
        MenuOptionEntity deletedOption = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("삭제된 옵션")
                .detail("안보여야 함")
                .price(0)
                .createdBy(CREATOR_ID)
                .build();
        menuOptionRepository.save(deletedOption);

        // 2. 삭제 처리
        deletedOption.softDelete(EDITOR_ID);
        menuOptionRepository.save(deletedOption);

        // 3. 조회 (삭제된 것 제외)
        List<MenuOptionEntity> customerOptions = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(savedMenu.getId());

        System.out.println("\n========== [테스트 2] 손님용 조회 결과 (삭제 제외) ==========");
        printOptions(customerOptions); // 출력 헬퍼 메서드 호출

        // 4. 검증
        assertThat(customerOptions)
                .extracting("name", "isDeleted", "isAvailable")
                .contains(
                        tuple("육전 추가", false, true),
                        tuple("면 추가", false, false)
                )
                .doesNotContain(
                        tuple("삭제된 옵션", true, true)
                );
    }

    @Test
    @DisplayName("[관리자] 삭제된 옵션까지 포함하여 모든 옵션을 조회")
    void 관리자_옵션_전체_조회() {
        // 1. 삭제할 옵션 생성
        MenuOptionEntity deletedOption = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("삭제된 옵션")
                .detail("데이터 복구용")
                .price(0)
                .createdBy(CREATOR_ID)
                .build();
        menuOptionRepository.save(deletedOption);

        // 2. 삭제 처리
        deletedOption.softDelete(EDITOR_ID);
        menuOptionRepository.save(deletedOption);

        // 3. 조회 (삭제된 것 포함 전체)
        List<MenuOptionEntity> adminOptions = menuOptionRepository.findAllByMenuId(savedMenu.getId());

        System.out.println("\n========== [테스트 3] 관리자용 조회 결과 (전체 포함) ==========");
        printOptions(adminOptions); // 출력 헬퍼 메서드 호출

        // 4. 검증
        assertThat(adminOptions)
                .hasSize(3) // 기존2 + 삭제된거1
                .extracting("name", "isDeleted", "deletedBy")
                .contains(
                        tuple("육전 추가", false, null),
                        tuple("면 추가", false, null),
                        tuple("삭제된 옵션", true, EDITOR_ID) // 여기서는 보여야 함!
                );
    }

    // Helper 메서드
    private void printOptions(List<MenuOptionEntity> options) {
        if (options.isEmpty()) {
            System.out.println("조회된 옵션이 없습니다.");
            return;
        }
        for (MenuOptionEntity opt : options) {
            String status;
            String userAction; // 누가 마지막에 건드렸는지 설명
            Integer userId;    // 그 사람의 ID

            if (opt.getIsDeleted()) {
                status = "[삭제됨]";
                userAction = "삭제";
                userId = opt.getDeletedBy(); // ★ 삭제된 경우 deletedBy 사용
            } else if (!opt.getIsAvailable()) {
                status = "[품절]";
                userAction = "수정";
                userId = opt.getUpdatedBy(); // ★ 수정된 경우 updatedBy 사용
            } else {
                status = "[판매중]";
                userAction = "등록";
                userId = opt.getCreatedBy(); // ★ 최초 상태는 createdBy 사용
            }

            System.out.println(String.format("%-10s | %s | %s자: %s",
                    status, opt.getName(), userAction, userId));
        }
    }
}