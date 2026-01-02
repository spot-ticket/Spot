package com.example.Spot.store.domain.repository;

import com.example.Spot.store.domain.entity.StoreEntity;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void 가게를_저장하고_조회할_수_있다() {
        //given (준비)
        StoreEntity store = StoreEntity.builder()
                .name("테스트 가게")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();
        //when (실행)
        StoreEntity savedStore = storeRepository.save(store);

        //then (검증)
        Optional<StoreEntity> foundStore = storeRepository.findById(savedStore.getId());
        assertThat(foundStore).isPresent();
        assertThat(foundStore.get().getName()).isEqualTo("테스트 가게");
    }

    @Test
    void 모든_가게를_조회할_수_있다() {
        //given
        StoreEntity store1 = StoreEntity.builder()
                .name("치킨집")
                .address("서울시 강남구")
                .phoneNumber("02-1111-1111")
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        StoreEntity store2 = StoreEntity.builder()
                .name("피자집")
                .address("서울시 서초구")
                .phoneNumber("02-2222-2222")
                .openTime(LocalTime.of(12, 0))
                .closeTime(LocalTime.of(3, 0))
                .build();

        storeRepository.save(store1);
        storeRepository.save(store2);

        //when
        List<StoreEntity> stores = storeRepository.findAll();

        //then
        assertThat(stores).hasSize(2);
        assertThat(stores)
                .extracting(StoreEntity::getName)
                .containsExactlyInAnyOrder("치킨집", "피자집");

    }

    @Test
    void 가게를_soft_delete_할_수_있다() {
        //given
        StoreEntity store = StoreEntity.builder()
                .name("삭제될 가게")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(23, 0))
                .build();

        StoreEntity savedStore = storeRepository.save(store);

        //when
        savedStore.softDelete();
        StoreEntity deletedStore = storeRepository.save(savedStore);

        //then
        assertThat(deletedStore.getIsDeleted()).isTrue();

        Optional<StoreEntity> foundStore = storeRepository.findById(deletedStore.getId());
        assertThat(foundStore).isPresent();
        assertThat(foundStore.get().getIsDeleted()).isTrue();
    }

    @Test
    void 삭제되지_않은_가게만_조회할_수_있다() {
        //given
        StoreEntity activeStore = StoreEntity.builder()
                .name("영업중인 가게")
                .address("서울시 강남구")
                .phoneNumber("02-1111-1111")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .build();
        StoreEntity deletedStore = StoreEntity.builder()
                .name("삭제된 가게")
                .address("서울시 서초구")
                .phoneNumber("02-2222-2222")
                .openTime(LocalTime.of(13, 0))
                .closeTime(LocalTime.of(2, 0))
                .build();

        storeRepository.save(activeStore);

        StoreEntity saved = storeRepository.save(deletedStore);
        saved.softDelete();
        storeRepository.save(saved);

        //when
        List<StoreEntity> activeStores = storeRepository.findByIsDeletedFalse();

        //then
        assertThat(activeStores).hasSize(1);
        assertThat(activeStores.get(0).getName()).isEqualTo("영업중인 가게");
        assertThat(activeStores.get(0).getIsDeleted()).isFalse();
    }

    @Test
    void ID로_삭제되지_않은_가게를_조회할_수_있다() {
        //given
        StoreEntity store = StoreEntity.builder()
                .name("영업중인 가게")
                .address("서울시 강남구")
                .phoneNumber("02-123-5678")
                .openTime(LocalTime.of(13, 0))
                .closeTime(LocalTime.of(4, 0))
                .build();

        StoreEntity savedStore = storeRepository.save(store);

        //when
        Optional<StoreEntity> foundStore = storeRepository.findByIdAndIsDeletedFalse(savedStore.getId());

        //then
        assertThat(foundStore).isPresent();
        assertThat(foundStore.get().getName()).isEqualTo("영업중인 가게");
    }

    @Test
    void 삭제된_가게는_ID로_조회되지_않는다() {
        //given
        StoreEntity store = StoreEntity.builder()
                .name("삭제된 가게")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .openTime(LocalTime.of(18, 0))
                .closeTime(LocalTime.of(7, 0))
                .build();

        StoreEntity savedStore = storeRepository.save(store);
        savedStore.softDelete();
        storeRepository.save(savedStore);

        //when
        Optional<StoreEntity> foundStore = storeRepository.findByIdAndIsDeletedFalse(savedStore.getId());

        //then
        assertThat(foundStore).isEmpty(); //삭제되니 가게는 조회되지 말야아 함
    }
}