package com.example.Spot.menu.domain.repository;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MenuRepositoryTest {
    @Autowired
    MenuRepository menuRepository;

    @Autowired
    StoreRepository storeRepository;    // storeRepository를 사용하기 위해 @Autowired 어노테이션을 이용하여 Bean을 주입함

    @Test
    @DisplayName("가게 및 메뉴 등록 테스트")
    void createMenuTest() {
        // [Given] 1. StoreEntity 생성 및 저장
        StoreEntity store = StoreEntity.builder()
                .name("원조역삼막국수")
                .address("서울시 강남구")
                .phoneNumber("02-4321-8765")
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        // Store의 ID(FK)가 필요하므로 먼저 저장
        StoreEntity savedStore = storeRepository.save(store);

        // [Given] 2. MenuEntity 생성 (위에서 저장한 savedStore를 넣어줌)
        MenuEntity menu = MenuEntity.builder()
                .store(savedStore)
                .name("육전물막국수")
                .category("한식")
                .price(13000)
                .build();

        // [When] 메뉴 저장 실행
        MenuEntity savedMenu = menuRepository.save(menu);

        // [Then] 검증
        // 1. 메뉴가 잘 저장되어 ID가 생성되었는지 확인
        assertThat(savedMenu.getId()).isNotNull();

        // 2. 등록한 메뉴의 내용이 맞는지 확인
        assertThat(savedMenu.getName()).isEqualTo("육전물막국수");
        assertThat(savedMenu.getCategory()).isEqualTo("한식");
        assertThat(savedMenu.getPrice()).isEqualTo(13000);

        // 3. 등록한 메뉴가 만든 가게에 잘 속해있는지 확인
        assertThat(savedMenu.getStore().getId()).isEqualTo(savedStore.getId());
        assertThat(savedMenu.getStore().getName()).isEqualTo(savedStore.getName());
    }
}

