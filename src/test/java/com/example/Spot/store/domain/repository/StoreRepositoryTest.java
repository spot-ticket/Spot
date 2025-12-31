package com.example.Spot.store.domain.repository;

import com.example.Spot.store.domain.entity.StoreEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;
import java.util.List;
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

        StoreRepository.save(activeStore);

        StoreEntity saved = storeRepository.save(deletedStore);
        saved.deleted();
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
    void 삭제된_가게는_ID로_조회되지_않는다(){
        //given
        StoreEntity Store = StoreEntity.builder()
                .name("삭제된 가게")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .openTime(LocalTime.of(18, 0))
                .closeTime(LocalTime.of(7, 0))
                .build();

        StoreEntity savedStore = storeRepository.save(store);
        savedStore.delete();
        storeRepository.save(savedStore);

        //when
        Optional<StoreEntity> foundStore = storeRepository.findByIdAndIsDeletedFalse(savedStore.getId());

        //then
        assertThat(foundStore).isEmpty(); //삭제되니 가게는 조회되지 말야아 함
    }

    @Test
    void 특정_Owner의_모든_가게를_조회할_수_있다(){
        //given
        Long ownerId1 = 100L;
        Long ownerId2 = 200L;

        StoreEntity store1 = StoreEntity.builder()
                .name("오너1의 첫번째 가게")
                .address("서울시 강남구")
                .phoneNumber("02-1111-1111")
                .ownerUserId(owenrId1)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(20, 0))
                .build();

        StoreEntity store2 = StoreEntity.builder()
                .name("오너1의 두번째 가게")
                .address("서울시 서초구")
                .ownerUserId(ownerId1)
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(21, 0))
                .build();

        StoreEntity store3 = StoreEntity.builder()
                .name("오너2의 가게")
                .address("서울시 송파구")
                .phoneNumber("02-3333-3333")
                .ownerUserId(ownerId2)
                .openTime(LocalTime.of(6,0))
                .closeTIme(LocalTime.of(18, 0))
                .build();

        storeRepository.save(store1);
        storeRepository.save(store2);
        storeRepository.save(store3);

        // when
        List<StoreEntity> owner1Stores = storeRepository.findByOwnerUserId(ownerId1);

        // then
        assertThat(owner1Stores).hasSize(2);
        assertThat(owner1Stores).extracting("name")
                .containExactlyInAnyOrder("오너1의 첫번째 가게", "오너1의 두번째 가게");
        assertThat(owner1Stores).allMatch(store -> store.getOwnerUserId.equals(ownerId1));
    }

    @Test
    void 특정_Owner의_삭제되지_않은_가게만_조회할_수_있다(){
        //given
        Long ownerId = 100L;

        StoreEntity activeStore = StoreEntity.builder()
                .name("영업중인 가게")
                .address("서울시 강남구")
                .phoneNumber("02-1111-1111")
                .ownerUserId(ownerId)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();

        StoreEntity deletedStore = StoreEntity.builder()
                .name("삭제된 가게")
                .address("서울시 서초구")
                .phoneNumber("02-2222-2222")
                .ownerUserId(ownerId)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();

        storeRepository.save(activeStore);

        StoreEntity saved = storeRepository.save(deletedStore);
        saved.delete();
        storeRepository.save(saved);

        //when
        List<StoreEntity> activeStores = storeRepository.findByOwnerUserIdAndIsDeletedFalse(ownerId);

        //then
        assertThat(activeStores)
                .hasSize(1)
                .first()
                .satisfies(store -> {
                    assertThat(store.getName()).isEqualTo("영업중인 가게");
                    assertThat(store.getIsDeleted()).isFalse();
                });
    }

}