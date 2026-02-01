package com.example.Spot.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.store.application.service.StoreService;
import com.example.Spot.store.application.service.UserCallService;
import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreCategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.infrastructure.aop.StoreValidationContext;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private StoreCategoryRepository storeCategoryRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private UserCallService userCallService;

    @InjectMocks
    private StoreService storeService;

    // @Value로 들어오는 지역 정보를 테스트용으로 설정
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storeService, "activeRegions", List.of("서울", "경기"));
    }

    @Test
    @DisplayName("매장 생성 성공 테스트")
    void createStoreSuccess() {
        // given
        StoreCreateRequest request = new StoreCreateRequest("맛나식당", "서울시 강남구", "101호", "02-123-4567", LocalTime.of(9, 0), LocalTime.of(22, 0), List.of("한식"), 1, 2);
        CategoryEntity mockCategory = CategoryEntity.builder().name("한식").build();

        given(categoryRepository.findAllByNameInAndIsDeletedFalse(anyList()))
                .willReturn(List.of(mockCategory));
        given(storeRepository.saveAndFlush(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UUID storeId = storeService.createStore(request, 1);

        // then
        verify(storeRepository).saveAndFlush(any());
        verify(storeCategoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("매장 상세 조회 - 비서비스 지역인 경우 예외 발생")
    void getStoreDetailsFailRegion() {
        // given
        UUID storeId = UUID.randomUUID();
        StoreEntity store = StoreEntity.builder().roadAddress("부산시 해운대구").build(); // '서울/경기'가 아님

        given(storeRepository.findByIdWithDetails(storeId, false)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.getStoreDetails(storeId, 1, false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("제공되지 않는 지역");
    }
    
    @Test
    @DisplayName("매장 정보 수정 성공")
    void updateStoreSuccess() {
        // given
        UUID storeId = UUID.randomUUID();
        StoreEntity store = spy(StoreEntity.builder().name("옛날이름").build());
        StoreUpdateRequest request = new StoreUpdateRequest("새이름", "서울", "상세", "010", LocalTime.of(10,0), LocalTime.of(20,0), null);

        // AOP에서 컨텍스트에 저장했다고 가정
        StoreValidationContext.setCurrentStore(store);

        // when
        storeService.updateStore(storeId, request, 1);

        // then
        assertThat(store.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("매장 삭제 - 권한 없는 사용자가 시도 시 예외")
    void deleteStoreFailAuth() {
        // given
        UUID storeId = UUID.randomUUID();
        StoreEntity store = StoreEntity.builder().build(); // 빈 유저 리스트 (주인 없음)

        given(storeRepository.findByIdWithDetailsForOwnerWithLock(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> storeService.deleteStore(storeId, 999, false))
                .isInstanceOf(AccessDeniedException.class);
    }
}