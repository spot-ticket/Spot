package com.example.Spot.store.application.service;

import java.util.List;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.feign.UserClient;
import com.example.Spot.global.presentation.advice.DuplicateResourceException;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.StoreStatus;
import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.infrastructure.aop.AdminOnly;
import com.example.Spot.store.infrastructure.aop.StoreValidationContext;
import com.example.Spot.store.infrastructure.aop.ValidateStoreAuthority;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUserUpdateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final UserCallService userCallService;
    
    // ******* //
    // 매장 생성 //
    // ******* //
    @Transactional
    public UUID createStore(StoreCreateRequest dto, Integer userId) {

        // user 상태 검증
        userCallService.validateActiveUser(userId);

        if (storeRepository.existsByRoadAddressAndAddressDetailAndNameAndIsDeletedFalse(
                dto.roadAddress(), dto.addressDetail(), dto.name())) {
            throw new DuplicateResourceException(
                    String.format("이미 존재하는 매장입니다. (주소: %s %s, 매장명: %s)",
                        dto.roadAddress(), dto.addressDetail(), dto.name())
            );
        }
        
        List<CategoryEntity> categories = dto.categoryNames().stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다.: " + name)))
                .toList();
        
        StoreEntity store = dto.toEntity(categories);

        store.addStoreUser(dto.ownerId());
        store.addStoreUser(dto.chefId());

        return storeRepository.save(store).getId();
    }


    // *********** //
    // 매장 상세 조회 //
    // *********** //
    public StoreDetailResponse getStoreDetails(UUID storeId, Integer userId, boolean isAdmin) {

        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        if (!isAdmin) {
            validateServiceRegion(store.getRoadAddress());
        }

        List<MenuEntity> menuEntities = menuRepository.findAllActiveMenus(storeId);
        List<MenuPublicResponseDto> menus = menuEntities.stream()
                .map(menu -> MenuPublicResponseDto.of(menu, menu.getOptions()))
                .toList();

        return StoreDetailResponse.fromEntity(store, menus);
    }

    public Page<StoreListResponse> getAllStores(boolean isAdmin, Pageable pageable) {

        Page<StoreEntity> stores = storeRepository.findAllByRole(isAdmin, pageable);

        return convertToPageResponse(stores, isAdmin, pageable);
    }


    // ******* //
    // 매장 변경 //
    // ******* //
    @Transactional
    @ValidateStoreAuthority
    public void updateStore(UUID storeId, StoreUpdateRequest request, Integer userId) {
        StoreEntity store = StoreValidationContext.getCurrentStore();
        
        List<CategoryEntity> categories = null;
        if (request.categoryNames() != null) {
            categories = request.categoryNames().stream()
                    .map(name -> categoryRepository.findByName(name)
                            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리: " + name)))
                    .toList();
        }
        
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
    

    // ************ //
    // 매장 스태프 변경 //
    // ************ //
    @Transactional
    @ValidateStoreAuthority
    public void updateStoreStaff(UUID storeId, StoreUserUpdateRequest request, Integer userId) {
        StoreEntity store = StoreValidationContext.getCurrentStore();

        for (StoreUserUpdateRequest.UserChange change : request.changes()) {
            if (change.action() == StoreUserUpdateRequest.Action.ADD) {
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

    // ******* //
    // 매장 삭제 //
    // ******* //
    @Transactional
    public void deleteStore(UUID storeId, Integer userId, boolean isAdmin) {

        StoreEntity store;

        // Admin은 모두 삭제 가능함.                
        if (isAdmin) {
            store = storeRepository.findByIdWithDetails(storeId, true)
                    .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));
        } else {

            store = storeRepository.findByIdWithDetailsForOwner(storeId)
                    .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

            boolean isRealOwner = store.getUsers().stream()
                    .anyMatch(su -> su.getUserId().equals(userId));

            if (!isRealOwner) {
                throw new AccessDeniedException("해당 매장에 대한 관리 권한이 없습니다.");
            }
        }

        store.softDelete(userId);
    }
    

    public Page<StoreListResponse> searchStoresByName(String keyword, boolean isAdmin, Pageable pageable) {

        Page<StoreEntity> stores = storeRepository.searchByName(keyword, isAdmin, pageable);

        return convertToPageResponse(stores, isAdmin, pageable);
    }

    // ********* //
    // 내 매장 조회 //
    // ********* //
    public List<StoreListResponse> getMyStores(Integer userId) {

        List<StoreEntity> stores = storeRepository.findAllByOwnerId(userId);

        return stores.stream()
                .map(StoreListResponse::fromEntity)
                .toList();
    }

    // *********** //
    // 매장 상태 변경 //
    // *********** //
    @AdminOnly
    @Transactional
    public void updateStoreStatus(UUID storeId, StoreStatus status, Integer userId) {

        StoreEntity store = storeRepository.findByIdWithDetails(storeId, true)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        store.updateStatus(status);
    }
    
    private boolean isServiceable(String roadAddress) {
        return activeRegions.stream().anyMatch(roadAddress::contains);
    }

    private void validateServiceRegion(String roadAddress) {
        if (!isServiceable(roadAddress)) {
            throw new AccessDeniedException("현재 픽업 서비스가 제공되지 않는 지역의 매장입니다.");
        }
    }

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
