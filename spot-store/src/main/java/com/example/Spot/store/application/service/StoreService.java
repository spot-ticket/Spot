package com.example.spotstore.store.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.presentation.advice.DuplicateResourceException;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.domain.repository.StoreUserRepository;
import com.example.Spot.store.infrastructure.aop.AdminOnly;
import com.example.Spot.store.infrastructure.aop.StoreValidationContext;
import com.example.Spot.store.infrastructure.aop.ValidateStoreAuthority;
import com.example.Spot.store.infrastructure.aop.ValidateUser;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUserUpdateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    @Value("${service.active-regions}")
    private List<String> activeRegions;

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final StoreUserRepository storeUserRepository;
    
    @Transactional
    public UUID createStore(StoreCreateRequest dto, Integer userId) {
        
        if (storeRepository.existsByRoadAddressAndAddressDetailAndNameAndIsDeletedFalse(
                dto.roadAddress(), dto.addressDetail(), dto.name())) {
            throw new DuplicateResourceException(
                    String.format("이미 존재하는 매장입니다. (주소: %s %s, 매장명: %s)",
                        dto.roadAddress(), dto.addressDetail(), dto.name())
            );
        }
        
        // 1.1 DTO에 넘겨줄 카테고리 리스트를 생성
        List<CategoryEntity> categories = dto.categoryNames().stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다.: " + name)))
                .toList();
        
        // 1.2 매장 엔티티 생성(DTO -> Entity)
        StoreEntity store = dto.toEntity(categories);

        // 1.3 DTO에 담겨온 유저 ID로 스태프 등록 (MSA 전환으로 userId만 저장)
        store.addStoreUser(dto.ownerId());
        store.addStoreUser(dto.chefId());

        return storeRepository.save(store).getId();
    }

    // 2. 매장 상세 조회
    public StoreDetailResponse getStoreDetails(UUID storeId, Integer userId, boolean isAdmin) {

        // 2.1 레포지토리 호출 (권한은 외부에서 전달받음 - MSA 전환)
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        // 2.2 서비스 가능 지역인지 검증
        if (!isAdmin) {
            validateServiceRegion(store.getRoadAddress());
        }

        // 2.3 메뉴 목록 조회
        List<MenuEntity> menuEntities = menuRepository.findAllActiveMenus(storeId);
        List<MenuPublicResponseDto> menus = menuEntities.stream()
                .map(menu -> MenuPublicResponseDto.of(menu, menu.getOptions()))
                .toList();

        // 2.4 Entity를 DTO(Response)로 변환하여 반환
        return StoreDetailResponse.fromEntity(store, menus);
    }

    // 3. 매장 전체 조회
    public Page<StoreListResponse> getAllStores(boolean isAdmin, Pageable pageable) {
        // 3.1 레포지토리 호출 (관리자는 삭제된 것 포함, 권한은 외부에서 전달받음 - MSA 전환)
        Page<StoreEntity> stores = storeRepository.findAllByRole(isAdmin, pageable);

        // 3.2 서비스지역 기반 필터링 페이지네이션
        return convertToPageResponse(stores, isAdmin, pageable);
    }

    // 4. 매장 기본 정보 수정
    @Transactional
    @ValidateStoreAuthority
    public void updateStore(UUID storeId, StoreUpdateRequest request, Integer userId) {
        // AOP에서 검증한 엔티티를 Context에서 가져옴 (DB 재조회 없음)
        StoreEntity store = StoreValidationContext.getCurrentStore();
        
        // 4.2 카테고리 이름 리스트를 엔티티 리스트로 변환
        List<CategoryEntity> categories = null;
        if (request.categoryNames() != null) {
            categories = request.categoryNames().stream()
                    .map(name -> categoryRepository.findByName(name)
                            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리: " + name)))
                    .toList();
        }
        
        // 4.3 엔티티 내부 메서드 호출
        store.updateStoreDetails(
                request.name(),
                request.roadAddress(),
                request.addressDetail(),
                request.phoneNumber(),
                request.openTime(),
                request.closeTime(),
                categories
        );
    }
    
    // 5. 매장 직원 정보 수정
    @Transactional
    @ValidateStoreAuthority
    public void updateStoreStaff(UUID storeId, StoreUserUpdateRequest request, Integer userId) {
        StoreEntity store = StoreValidationContext.getCurrentStore();

        for (StoreUserUpdateRequest.UserChange change : request.changes()) {
            if (change.action() == StoreUserUpdateRequest.Action.ADD) {
                // 중복 체크 (MSA 전환으로 userId로 직접 비교)
                boolean alreadyStaff = store.getUsers().stream()
                        .anyMatch(su -> su.getUserId().equals(change.userId()));

                if (!alreadyStaff) {
                    store.addStoreUser(change.userId());
                }
            } else if (change.action() == StoreUserUpdateRequest.Action.REMOVE) {
                store.getUsers().removeIf(su -> su.getUserId().equals(change.userId()));
            }
        }
    }

    // 6. 매장 삭제
    @Transactional
    public void deleteStore(UUID storeId, Integer userId, boolean isAdmin) {
        // 6.1 가게 조회 (권한은 외부에서 전달받음 - MSA 전환)
        StoreEntity store;
        if (isAdmin) {
            // 관리자는 모든 가게 조회 가능
            store = storeRepository.findByIdWithDetails(storeId, true)
                    .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));
        } else {
            // OWNER는 모든 상태(PENDING, APPROVED, REJECTED)의 자신의 가게 조회 가능
            store = storeRepository.findByIdWithDetailsForOwner(storeId)
                    .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

            // OWNER 본인 확인 (MSA 전환으로 userId로 직접 비교)
            boolean isRealOwner = store.getUsers().stream()
                    .anyMatch(su -> su.getUserId().equals(userId));

            if (!isRealOwner) {
                throw new AccessDeniedException("해당 매장에 대한 관리 권한이 없습니다.");
            }
        }

        // 6.2 소프트 삭제
        store.softDelete(userId);
    }
    
    // 7. 매장 이름으로 검색
    public Page<StoreListResponse> searchStoresByName(String keyword, boolean isAdmin, Pageable pageable) {
        // 7.1 레포지토리 호출 (권한은 외부에서 전달받음 - MSA 전환)
        Page<StoreEntity> stores = storeRepository.searchByName(keyword, isAdmin, pageable);

        // 7.2 서비스지역 기반 필터링 페이지네이션
        return convertToPageResponse(stores, isAdmin, pageable);
    }

    // 8. 내 가게 목록 조회 (OWNER, CHEF)
    @ValidateUser
    public List<StoreListResponse> getMyStores(Integer userId) {
        // 권한 검증은 Controller 또는 Gateway에서 수행 (MSA 전환)
        List<StoreEntity> stores = storeRepository.findAllByOwnerId(userId);

        return stores.stream()
                .map(StoreListResponse::fromEntity)
                .toList();
    }

    // 9. 가게 승인 상태 변경 (MANAGER, MASTER만 가능)
    @Transactional
    @AdminOnly
    public void updateStoreStatus(UUID storeId, com.example.Spot.store.domain.StoreStatus status, Integer userId) {
        // AOP에서 관리자 권한 검증 완료
        // 가게 조회 (관리자는 삭제된 가게도 조회 가능)
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, true)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        store.updateStatus(status);
    }
    
    // ----- [공통 검증 로직] -----
    // 1. 서비스 가능한 지역 여부 확인
    private boolean isServiceable(String roadAddress) {
        return activeRegions.stream().anyMatch(roadAddress::contains);
    }

    // 2. 서비스 지역 검증(예외 발생) - 상세 조회에서 서비스 불가능 지역일 경우 접근 차단 후 에러 메시지 출력
    private void validateServiceRegion(String roadAddress) {
        if (!isServiceable(roadAddress)) {
            throw new AccessDeniedException("현재 픽업 서비스가 제공되지 않는 지역의 매장입니다.");
        }
    }

    // 3. 서비스지역기반 필터링 공통 메서드
    private Page<StoreListResponse> convertToPageResponse(Page<StoreEntity> stores, boolean isAdmin, Pageable pageable) {
        if (isAdmin) {
            return stores.map(StoreListResponse::fromEntity);
        }
        List<StoreListResponse> filteredContent = stores.getContent().stream()
                .filter(store -> isServiceable(store.getRoadAddress()))
                .map(StoreListResponse::fromEntity)
                .toList();

        return new PageImpl<>(filteredContent, pageable, filteredContent.size());
    }
}
