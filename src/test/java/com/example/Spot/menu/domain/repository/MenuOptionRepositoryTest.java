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
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

@DataJpaTest
class MenuOptionRepositoryTest {
    @Autowired
    private StoreRepository storeRepository;

    private StoreEntity savedStore;

    @Autowired
    private MenuRepository menuRepository;

    private MenuEntity savedMenu;

    @Autowired
    private MenuOptionRepository menuOptionRepository;

    private MenuOptionEntity savedOption;   // 정상 옵션
    private MenuOptionEntity soldOutOption; // 품절 옵션

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

        ReflectionTestUtils.setField(store, "createdBy", 123);
        ReflectionTestUtils.setField(store, "createdAt", LocalDateTime.now());
        // Store의 ID가 필요하므로 먼저 저장
        savedStore = storeRepository.save(store);

        // [Given] 2. MenuEntity 생성 및 저장
        MenuEntity menu = MenuEntity.builder()
                .store(savedStore)
                .name("육전막국수")
                .category("한식")
                .price(11000)
                .build();

        ReflectionTestUtils.setField(menu, "createdBy", 123);
        ReflectionTestUtils.setField(menu, "createdAt", LocalDateTime.now());
        savedMenu = menuRepository.save(menu);

        // 구매 가능 옵션
        MenuOptionEntity option = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("육전 추가")
                .detail("4조각")
                .price(4000)
                .build();

        ReflectionTestUtils.setField(option, "createdBy", 123);
        ReflectionTestUtils.setField(option, "createdAt", LocalDateTime.now());
        savedOption = menuOptionRepository.save(option);

        // 품절된 옵션
        MenuOptionEntity soldOption = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("면 추가")
                .detail("곱빼기")
                .price(2500)
                .build();

        ReflectionTestUtils.setField(soldOption, "createdBy", 123);
        ReflectionTestUtils.setField(soldOption, "createdAt", LocalDateTime.now());

        soldOption.changeAvailable(false);
        soldOutOption = menuOptionRepository.save(soldOption);
    }

    @Test
    @DisplayName("[손님/가게] 삭제된 옵션을 제외한 모든 옵션 조회 테스트")
    void 옵션_제외_조회() {
        // 1. 삭제된 옵션 생성
        MenuOptionEntity deletedOption = MenuOptionEntity.builder()
                .menu(savedMenu)
                .name("삭제된 옵션")
                .detail("삭제 테스트용")
                .price(1)
                .build();
        deletedOption.softDelete(0);

        ReflectionTestUtils.setField(deletedOption, "createdBy", 123);
        ReflectionTestUtils.setField(deletedOption, "createdAt", LocalDateTime.now());

        menuOptionRepository.save(deletedOption);

        // 2. When
        List<MenuOptionEntity> storeOptions = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(savedMenu.getId());

        // 3. Then
        assertThat(storeOptions)
                .extracting("name", "isDeleted", "isAvailable")
                .contains(
                        // 품절된("면 추가") 것도 잘 나오는지 확인 (Good!)
                        tuple("육전 추가", false, true),
                        tuple("면 추가", false, false)
                )
                .doesNotContain(
                        // [수정] 5개 개수를 맞춰줘야 비교가 가능합니다.
                        tuple("삭제된 옵션", true, true)
                );
    }
}
