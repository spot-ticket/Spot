package com.example.Spot.store.domain.repository;

import com.example.Spot.store.domain.entity.StoreEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;
import java.util.Optional;

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
    void 모든_가게를_조회할_수_있다(){
        //given
        StoreEntity store1 = StoreEntity.builder()
                .name("치킨집")
                .address("서울시 강남구")
                .phoneNumber("02-1111-1111")
                .openTime(LocalTime.of(10,0))
                .closeTime(LocalTime.of(21,0))
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
        List(StoreEntity) stores = storeRepository.findAll();

        //then
        assertThat(stores).hasSize(2);
        assertThat(stores).extracting("name")
                .containExactlyInAnyOrder("치킨집", "피자집");

    }

    @Test
    void 가게를_soft_delete_할_수_있다(){
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
        savedStore.delete();
        StoreEntity deletedStore = storeRepository.save(savedStore);

        //then
        assertThat(deletedStore.getIsDeleted()).isTrue();

        Optional<StoreEntity> foundStore = storeRepository.findById(deletedStore.getId());
        assertThat(foundStore).isPresent();
        assertThat(foundStore.get().getIsDeleted()).isTrue();
    }
}