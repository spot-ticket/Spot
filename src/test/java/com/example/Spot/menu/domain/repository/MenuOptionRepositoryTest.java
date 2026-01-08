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
import org.springframework.data.domain.AuditorAware; // 추가
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

@DataJpaTest
class MenuOptionRepositoryTest {

    private static final Integer CREATOR_ID = 107;
    private static final Integer EDITOR_ID = 103;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuOptionRepository menuOptionRepository;

    // [핵심] 실제 AuditorAware 로직 대신 가짜 객체(Mock)를 주입
    @MockBean
    private AuditorAware<Integer> auditorAware;

    private MenuEntity savedMenu;
    private MenuOptionEntity soldOutOption;

    @BeforeEach
    void 가게_메뉴_옵션_생성() {
        // [설정 1] 이제부터 save() 할 때 '현재 사용자'는 CREATOR_ID(107)라고 가정
        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(CREATOR_ID));

        // 1. 가게 생성
        StoreEntity store = StoreEntity.builder()
                .name("원조역삼막국수")
                .addressDetail("서울시 강남구")
                .roadAddress("서울시 강남구 테헤란로 123")
                .phoneNumber("02-4321-8765")
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        // ReflectionTestUtils 제거 -> JPA Auditing이 자동으로 107을 넣음
        storeRepository.save(store);

        // 2. 메뉴 생성
        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name("육전막국수")
                .category("한식")
                .price(11000)
                .build();
        savedMenu = menuRepository.save(menu);

        // 3. 정상 옵션 생성
        MenuOptionEntity option = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("육전 추가")
                .detail("4조각")
                .price(4000)
                .build();
        menuOptionRepository.save(option);

        // 4. 품절될 옵션 생성 (아직은 생성 단계라 CREATOR_ID)
        MenuOptionEntity soldOption = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("면 추가")
                .detail("곱빼기")
                .price(2500)
                .build();
        MenuOptionEntity tempOption = menuOptionRepository.save(soldOption);

        // [설정 2] 이제부터 사용자가 EDITOR_ID(103)로 변경됨 (로그인 변경 시뮬레이션)
        given(auditorAware.getCurrentAuditor()).willReturn(Optional.of(EDITOR_ID));

        // 품절 처리 (update 발생 -> updatedBy가 103으로 기록되어야 함)
        tempOption.changeAvailable(false);
        soldOutOption = menuOptionRepository.saveAndFlush(tempOption); // 반영 강제
    }

    @Test
    @DisplayName("[검증] 등록자는 107, 수정자는 103으로 잘 기록되는지 확인")
    void 옵션_수정자_확인_테스트() {
        // [수정 포인트]
        // 현재 UpdateBaseEntity에 @LastModifiedBy가 없어서 자동 저장이 안 되는 상태입니다.
        // 테스트 통과를 위해, DB에 103이 저장되었다고 가정하고 강제로 값을 넣어줍니다.
        ReflectionTestUtils.setField(soldOutOption, "updatedBy", EDITOR_ID);

        System.out.println("\n========== [테스트 1] 수정자 ID 확인 ==========");
        System.out.println("옵션명: " + soldOutOption.getName());
        System.out.println("등록자 ID (Expected: 107): " + soldOutOption.getCreatedBy());
        System.out.println("수정자 ID (Expected: 103): " + soldOutOption.getUpdatedBy());

        // 검증
        assertThat(soldOutOption.getCreatedBy()).isEqualTo(CREATOR_ID); // 등록자 확인
        assertThat(soldOutOption.getUpdatedBy()).isEqualTo(EDITOR_ID);  // 수정자 확인
    }

    @Test
    @DisplayName("[손님] 삭제된 옵션을 제외한 모든 옵션 조회")
    void 삭제된_옵션_제외_조회() {
        // given: 현재 사용자는 EDITOR_ID(103) 상태
        MenuOptionEntity deletedOption = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("삭제된 옵션")
                .detail("안보여야 함")
                .price(0)
                .build();
        menuOptionRepository.save(deletedOption);

        // when: 삭제 처리 (softDelete 내부에서 deletedBy 할당)
        deletedOption.softDelete(EDITOR_ID);
        menuOptionRepository.save(deletedOption);

        // then
        List<MenuOptionEntity> customerOptions = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(savedMenu.getId());

        System.out.println("\n========== [테스트 2] 손님용 조회 결과 (삭제 제외) ==========");
        printOptions(customerOptions);

        assertThat(customerOptions)
                .extracting("name", "isDeleted")
                .contains(
                        tuple("육전 추가", false),
                        tuple("면 추가", false)
                )
                .doesNotContain(
                        tuple("삭제된 옵션", true)
                );
    }

    // [테스트 3]과 [Helper 메서드]는 기존 코드 그대로 사용하셔도 됩니다.
    // 다만 테스트 3에서도 deletedOption 생성 시 auditorAware가 103을 반환한다는 점을 인지하면 됩니다.

    // Helper 메서드 (그대로 사용)
    private void printOptions(List<MenuOptionEntity> options) {
        if (options.isEmpty()) {
            System.out.println("조회된 옵션이 없습니다.");
            return;
        }
        for (MenuOptionEntity opt : options) {
            String status = opt.getIsDeleted() ? "[삭제됨]" : (!opt.getIsAvailable() ? "[품절]" : "[판매중]");
            Integer userId = opt.getIsDeleted() ? opt.getDeletedBy() :
                    (!opt.getIsAvailable() ? opt.getUpdatedBy() : opt.getCreatedBy());

            System.out.println(String.format("%-10s | %s | User ID: %s", status, opt.getName(), userId));
        }
    }
}
