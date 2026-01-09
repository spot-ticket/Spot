package com.example.Spot.store.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.Spot.config.TestConfig;
import com.example.Spot.global.TestSupport;
import com.example.Spot.store.domain.entity.StoreEntity;

@Import(TestConfig.class)
@DataJpaTest
class StoreRepositoryTest extends TestSupport {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void 가게를_저장하고_조회할_수_있다() {
        //given (준비)
        StoreEntity store = createStore("테스트 가게");
        StoreEntity savedStore = storeRepository.save(store);

        Optional<StoreEntity> foundStore = storeRepository.findById(savedStore.getId());

        assertThat(foundStore).isPresent();
        assertThat(foundStore.get().getName()).isEqualTo("테스트 가게");
    }

    @Test
    void 관리자는_삭제된_가게를_포함하여_전체_페이지_조회가_가능하다() {
        // given
        storeRepository.save(createStore("정상 가게"));
        StoreEntity deleted = storeRepository.save(createStore("삭제된 가게"));
        deleted.softDelete(TEST_USER_ID);
        storeRepository.save(deleted);

        // when
        Page<StoreEntity> result = storeRepository.findAllByRole(false, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("정상 가게");
    }

    @Test
    void 일반_유저는_삭제되지_않은_가게만_페이지_조회가_가능하다() {
        // given
        storeRepository.save(createStore("정상 가게"));
        StoreEntity deleted = storeRepository.save(createStore("삭제된 가게"));
        deleted.softDelete(TEST_USER_ID);
        storeRepository.save(deleted);
        
        // when
        Page<StoreEntity> result = storeRepository.findAllByRole(false, PageRequest.of(0, 10));
        
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("정상 가게");
    }

    @Test
    void 키워드로_가게_이름을_검색할_수_있다() { 
        // given
        storeRepository.save(createStore("맛있는 치킨"));
        storeRepository.save(createStore("맛있는 피자"));
        
        // when
        Page<StoreEntity> result = storeRepository.searchByName("치킨", false, PageRequest.of(0, 10));
         
        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("맛있는 치킨");
    }

    @Test
    void 삭제되지_않은_매장은_일반유저_권한으로도_상세조회가_가능하다() {
        // given
        StoreEntity store = storeRepository.save(createStore("영업중인 가게"));
        
        // when - 일반 유저(isAdmin = false) 권한으로 조회
        Optional<StoreEntity> foundStore = storeRepository.findByIdWithDetails(store.getId(), false);
        
        // then
        assertThat(foundStore).isPresent();
        assertThat(foundStore.get().getName()).isEqualTo("영업중인 가게");
    }

    @Test
    void 삭제된_가게는_일반유저_권한으로_상세조회되지_않는다() {
        // given
        StoreEntity store = storeRepository.save(createStore("삭제된 가게"));
        store.softDelete(TEST_USER_ID);
        storeRepository.saveAndFlush(store);
        entityManager.clear();
        
        // when - 일반 유저
        Optional<StoreEntity> foundStore = storeRepository.findByIdWithDetails(store.getId(), false);
        
        // then
        assertThat(foundStore).isEmpty();
    }
    
    @Test
    void 삭제된_가게라도_관리자_권한으로는_상세조회가_가능하다() {
        // given
        StoreEntity store = storeRepository.save(createStore("삭제된 가게"));
        store.softDelete(TEST_USER_ID);
        storeRepository.saveAndFlush(store);
        entityManager.clear();
        
        // when
        Optional<StoreEntity> foundStore = storeRepository.findByIdWithDetails(store.getId(), true);
        
        // then
        assertThat(foundStore).isPresent();
        assertThat(foundStore.get().getIsDeleted()).isTrue();
    }
    
    // 반복되는 Store 생성을 위한 헬퍼 메서드
    private StoreEntity createStore(String name) {
        return StoreEntity.builder()
                .name(name)
                .roadAddress("서울시 강남구")
                .addressDetail("123-45")
                .phoneNumber("02-1234-5678")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();
    }
}
