package com.example.Spot.menu.domain.repository;

import static org.assertj.core.api.Assertions.assertThat; // 검증을 위한 AssertJ
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.entity.MenuEntity;

@DataJpaTest
class MenuOptionRepositoryTest {
    @Autowired
    private MenuOptionRepository menuOptionRepository;

    private MenuOptionEntity savedOption;

    @Autowired
    private MenuRepository menuRepository;

    private MenuEntity savedMenu;

    @BeforeEach
    void setUp() {
        // [Given] 1. MenuEntity 생성 및 저장
        MenuEntity menu = MenuEntity.builder()
                .name("육전막국수")
                .category("한식")
                .price(11000)
                .build();

        savedMenu = menuRepository.save(menu);

        MenuOptionEntity option = MenuOptionEntity.builder()
                .menu(menu)
                .name("육전 추가")
                .detail("4조각")
                .price(4000)
                .build();

        savedOption = menuOptionRepository.save(option);
    }

    @Test
    @DisplayName("손님 - 메뉴 옵션 조회 테스트")
    void findActiveOptionTest() {
        List<MenuOptionEntity> activeOptions = menuOptionRepository.findAllActiveOptions(savedMenu.getId());

        assertThat(activeOptions)
                .extracting("menu.name", "name", "detail", "price", "isDeleted", "isAvailable")
                .containsExactly(tuple("육전막국수", "육전 추가", "4조각", 4000, false, true));
    }
}
