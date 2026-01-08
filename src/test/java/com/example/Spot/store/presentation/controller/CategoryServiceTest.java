package com.example.Spot.store.presentation.controller;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.Spot.config.TestConfig;
import com.example.Spot.global.TestSupport;
import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreCategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreCategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;

@DataJpaTest
@Import(TestConfig.class)
class CategoryServiceTest extends TestSupport {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreCategoryRepository storeViewRepository;

    @Test
    void 카테고리_전체를_조회할_수_있다() {
        CategoryEntity chicken = CategoryEntity.builder()
                .name("치킨")
                .build();

        CategoryEntity japanese = CategoryEntity.builder()
                .name("일식")
                .build();

        categoryRepository.save(chicken);
        categoryRepository.save(japanese);

        StoreEntity bbq = StoreEntity.builder()
                .name("BBQ 강남점")
                .roadAddress("서울시 서초구")
                .addressDetail("123-45")
                .phoneNumber("02-1111-1111")
                .build();

        StoreEntity kyochon = StoreEntity.builder()
                .name("교촌 역삼점")
                .roadAddress("서울시 서초구")
                .addressDetail("123-45")
                .phoneNumber("02-2222-2222")
                .build();

        storeRepository.save(bbq);
        storeRepository.save(kyochon);

        StoreCategoryEntity view1 = StoreCategoryEntity.builder()
                .store(bbq)
                .category(chicken)
                .build();

        StoreCategoryEntity view2 = StoreCategoryEntity.builder()
                .store(kyochon)
                .category(chicken)
                .build();

        storeViewRepository.save(view1);
        storeViewRepository.save(view2);

        List<CategoryEntity> categories =
                categoryRepository.findAllByIsDeletedFalse();

        assertThat(categories).hasSize(2);

        CategoryEntity foundChicken = categories.stream()
                .filter(c -> c.getName().equals("치킨"))
                .findFirst()
                .orElseThrow();

        List<StoreCategoryEntity> chickenStores =
                storeViewRepository.findByCategoryAndIsDeletedFalse(foundChicken);

        assertThat(chickenStores)
                .hasSize(2)
                .extracting("store.name")
                .containsExactlyInAnyOrder("BBQ 강남점", "교촌 역삼점");
    }

    @Test
    void ID로_삭제되지_않은_카테고리를_조회할_수_있다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("치킨")
                .build();

        CategoryEntity savedCategory =
                categoryRepository.save(category);

        Optional<CategoryEntity> foundCategory =
                categoryRepository.findByIdAndIsDeletedFalse(savedCategory.getId());

        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("치킨");
        assertThat(foundCategory.get().getIsDeleted()).isFalse();
    }

    @Test
    void 삭제된_카테고리는_ID로_조회되지_않는다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("분식")
                .build();

        CategoryEntity savedCategory =
                categoryRepository.save(category);

        savedCategory.softDelete(TEST_USER_ID);
        categoryRepository.save(savedCategory);

        Optional<CategoryEntity> foundCategory =
                categoryRepository.findByIdAndIsDeletedFalse(savedCategory.getId());

        assertThat(foundCategory).isEmpty();
    }

    @Test
    void 카테고리_단일_조회시_해당_카테고리에_속한_매장을_조회할_수_있다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("커피")
                .build();

        categoryRepository.save(category);

        StoreEntity store1 = StoreEntity.builder()
                .name("스타벅스 강남점")
                .roadAddress("서울시 서초구")
                .addressDetail("123-45")
                .phoneNumber("02-1111-1111")
                .build();

        StoreEntity store2 = StoreEntity.builder()
                .name("투썸플레이스 역삼점")
                .roadAddress("서울시 역삼동")
                .addressDetail("123-45")
                .phoneNumber("02-2222-2222")
                .build();

        storeRepository.save(store1);
        storeRepository.save(store2);

        StoreCategoryEntity view1 = StoreCategoryEntity.builder()
                .store(store1)
                .category(category)
                .build();

        StoreCategoryEntity view2 = StoreCategoryEntity.builder()
                .store(store2)
                .category(category)
                .build();

        storeViewRepository.save(view1);
        storeViewRepository.save(view2);

        CategoryEntity foundCategory =
                categoryRepository.findByIdAndIsDeletedFalse(category.getId())
                        .orElseThrow();

        List<StoreCategoryEntity> stores =
                storeViewRepository.findByCategoryAndIsDeletedFalse(foundCategory);

        assertThat(stores)
                .hasSize(2)
                .extracting("store.name")
                .containsExactlyInAnyOrder("스타벅스 강남점", "투썸플레이스 역삼점");
    }

    @Test
    @Disabled("StoreView relationship not yet implemented")
    void 카테고리_단일_조회시_삭제된_매장은_포함되지_않는다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("디저트")
                .build();

        categoryRepository.save(category);

        StoreEntity activeStore = StoreEntity.builder()
                .name("설빙 강남점")
                .roadAddress("서울시 강남구")
                .addressDetail("123-45")
                .phoneNumber("02-3333-3333")
                .build();

        StoreEntity deletedStore = StoreEntity.builder()
                .name("폐업한 디저트 가게")
                .roadAddress("서울시 강남구")
                .addressDetail("123-45")
                .phoneNumber("02-4444-4444")
                .build();

        storeRepository.save(activeStore);

        StoreEntity savedDeletedStore = storeRepository.save(deletedStore);
        savedDeletedStore.softDelete(TEST_USER_ID);
        storeRepository.save(savedDeletedStore);

        storeViewRepository.save(
                StoreCategoryEntity.builder()
                        .store(activeStore)
                        .category(category)
                        .build()
        );

        storeViewRepository.save(
                StoreCategoryEntity.builder()
                        .store(savedDeletedStore)
                        .category(category)
                        .build()
        );

        List<StoreCategoryEntity> stores =
                storeViewRepository.findByCategoryAndIsDeletedFalse(category);

        assertThat(stores)
                .hasSize(1)
                .first()
                .satisfies(view -> {
                    assertThat(view.getStore().getName()).isEqualTo("설빙 강남점");
                    assertThat(view.getStore().getIsDeleted()).isFalse();
                });
    }

    @Test
    void 카테고리를_생성할_수_있다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("커피")
                .build();

        CategoryEntity savedCategory =
                categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("커피");
        assertThat(savedCategory.getIsDeleted()).isFalse();
        assertThat(savedCategory.getCreatedAt()).isNotNull();
        assertThat(savedCategory.getUpdatedAt()).isNotNull();
    }

    @Test
    void 카테고리_생성시_isDeleted는_false로_저장된다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("분식")
                .build();

        CategoryEntity saved =
                categoryRepository.save(category);

        assertThat(saved.getIsDeleted()).isFalse();
    }

    @Test
    void 여러_카테고리를_생성할_수_있다() {
        CategoryEntity category1 = CategoryEntity.builder()
                .name("치킨")
                .build();

        CategoryEntity category2 = CategoryEntity.builder()
                .name("일식")
                .build();

        categoryRepository.save(category1);
        categoryRepository.save(category2);

        List<CategoryEntity> categories =
                categoryRepository.findAll();

        assertThat(categories)
                .extracting("name")
                .containsExactlyInAnyOrder("치킨", "일식");
    }

    @Test
    void 삭제되지_않은_카테고리만_수정_대상이_된다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("분식")
                .build();

        CategoryEntity saved =
                categoryRepository.save(category);

        saved.softDelete(TEST_USER_ID);
        categoryRepository.save(saved);

        Optional<CategoryEntity> found =
                categoryRepository.findByIdAndIsDeletedFalse(saved.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void 카테고리를_soft_delete_할_수_있다() {
        CategoryEntity category = CategoryEntity.builder()
                .name("야식")
                .build();

        CategoryEntity saved =
                categoryRepository.save(category);

        saved.softDelete(TEST_USER_ID);
        categoryRepository.save(saved);

        CategoryEntity deleted =
                categoryRepository.findById(saved.getId()).orElseThrow();

        assertThat(deleted.getIsDeleted()).isTrue();
    }

    @Test
    void 삭제된_카테고리는_전체_조회에서_제외된다() {
        CategoryEntity active = CategoryEntity.builder()
                .name("분식")
                .build();

        CategoryEntity deleted = CategoryEntity.builder()
                .name("폐업카테고리")
                .build();

        categoryRepository.save(active);

        CategoryEntity savedDeleted =
                categoryRepository.save(deleted);
        savedDeleted.softDelete(TEST_USER_ID);
        categoryRepository.save(savedDeleted);

        List<CategoryEntity> categories =
                categoryRepository.findByIsDeletedFalse();

        assertThat(categories)
                .hasSize(1)
                .first()
                .satisfies(category -> {
                    assertThat(category.getName()).isEqualTo("분식");
                    assertThat(category.getIsDeleted()).isFalse();
                });
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}
