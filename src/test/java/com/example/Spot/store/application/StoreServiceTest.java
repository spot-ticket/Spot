package com.example.Spot.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.store.application.service.StoreService;
import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {
    
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private StoreService storeService;
    
    private final UUID testStoreId = UUID.randomUUID();
    private final Integer masterId = 1;
    private final Integer ownerId = 2;
    
    @BeforeEach
    void setUp() {
        // 1. @Value 주입
        ReflectionTestUtils.setField(storeService, "activeRegions", List.of("서울", "경기"));
    }
    
    @Test
    void 관리자는_서비스_지역이_아니어도_조회가_가능하다() {
        // 1. Given
        UserEntity master = createUser(masterId, Role.MASTER);
        StoreEntity store = createStore(testStoreId, "지방 매장", "강원도 강릉시");

        given(userRepository.findById(masterId)).willReturn(Optional.of(master));
        given(storeRepository.findByIdWithDetails(testStoreId, true)).willReturn(Optional.of(store));
        
        // 2. When
        StoreDetailResponse result = storeService.getStoreDetails(testStoreId, masterId);
        
        // 3. then
        assertThat(result.name()).isEqualTo("지방 매장");
        verify(storeRepository).findByIdWithDetails(testStoreId, true);
    }
    
    @Test
    void 일반_유저가_타_지역_매장_조회_시_예외가_발생한다() {
        // 1. Given
        UserEntity customer = createUser(3, Role.CUSTOMER);
        StoreEntity store = createStore(testStoreId, "지방 매장", "강원도 강릉시");

        given(userRepository.findById(3)).willReturn(Optional.of(customer));
        given(storeRepository.findByIdWithDetails(testStoreId, false)).willReturn(Optional.of(store));
        
        // 2. When & Then
        assertThatThrownBy(() -> storeService.getStoreDetails(testStoreId, 3))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("서비스가 제공되지 않는 지역");
    }
    
    @Test
    void 소유주가_본인_매장을_삭제하면_성공한다() {
        // 1. Given
        UserEntity owner = createUser(ownerId, Role.OWNER);
        StoreEntity store = createStore(testStoreId, "내 가게", "서울시");
        store.addStoreUser(owner);

        given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(storeRepository.findByIdWithDetails(testStoreId, false)).willReturn(Optional.of(store));
        
        // 2. When
        storeService.deleteStore(testStoreId, ownerId);
        
        // 3. Then
        assertThat(store.getIsDeleted()).isTrue();
        verify(storeRepository).findByIdWithDetails(testStoreId, false);
    }
    
    @Test
    void 소유주가_아닌_유저가_삭제하면_예외가_발생한다() {
        // 1. Given
        UserEntity otherOwner = createUser(99, Role.OWNER);
        UserEntity realOwner = createUser(ownerId, Role.OWNER);

        StoreEntity store = createStore(testStoreId, "진짜 사장님 가게", "서울시");
        store.addStoreUser(realOwner);

        given(userRepository.findById(99)).willReturn(Optional.of(otherOwner));
        given(storeRepository.findByIdWithDetails(testStoreId, false)).willReturn(Optional.of(store));
        
        // 2. When & Then
        assertThatThrownBy(() -> storeService.deleteStore(testStoreId, 99))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("관리 권한이 없습니다.");
    }
    
    @Test
    void 매장_생성_성공_테스트() {
        // 1. Given
        Integer userId = 1;
        UserEntity user = createUser(userId, Role.OWNER);
        // 가짜 DTO 생성
        StoreCreateRequest request = new StoreCreateRequest(
                "새로운 가게", "서울시 강남구", "101호", "02-123-4567",
                LocalTime.of(9, 0), LocalTime.of(22, 0), 
                List.of("한식"), userId, userId
        );
         
        given(userRepository.findById(anyInt())).willReturn(Optional.of(user));
        given(categoryRepository.findByName("한식")).willReturn(Optional.of(new CategoryEntity("한식")));
        given(storeRepository.save(any(StoreEntity.class))).willAnswer(invocation -> {
            StoreEntity store = invocation.getArgument(0);
            ReflectionTestUtils.setField(store, "id", UUID.randomUUID());
            return store;
        });
        
        // 2. When
        UUID resultId = storeService.createStore(request, userId);
        
        // 3. Then
        assertThat(resultId).isNotNull();
        verify(storeRepository).save(any(StoreEntity.class));
        verify(categoryRepository).findByName("한식");
    }
    
    @Test
    void 전체조회_일반유저는_서비스지역_매장만_볼_수_있다() {
        // 1. Given
        Integer userId = 10;
        UserEntity customer = createUser(userId, Role.CUSTOMER);
        
        // 서울 매장과 부산 매장 준비
        StoreEntity seoulStore = createStore(UUID.randomUUID(), "서울가게", "서울시 종로구");
        StoreEntity busanStore = createStore(UUID.randomUUID(), "부산가게", "부산시 해운대구");

        Page<StoreEntity> storePage = new PageImpl<>(List.of(seoulStore, busanStore));

        given(userRepository.findById(userId)).willReturn(Optional.of(customer));
        given(storeRepository.findAllByRole(false, PageRequest.of(0, 10))).willReturn(storePage);
        
        // 2. When
        Page<StoreListResponse> result = storeService.getAllStores(userId, PageRequest.of(0, 10));
        
        // 3. Then
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("서울가게");
    }
    
    // 공통 로직
    private UserEntity createUser(Integer id, Role role) {
        return UserEntity.forAuthentication(id, role);
    }
    
    private StoreEntity createStore(UUID id, String name, String address) {
        StoreEntity store = StoreEntity.builder()
                .name(name)
                .roadAddress(address)
                .build();

        ReflectionTestUtils.setField(store, "id", id);
        ReflectionTestUtils.setField(store, "storeCategoryMaps", new HashSet<>());
        
        return store;
    }
}
