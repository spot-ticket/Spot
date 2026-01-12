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

import com.example.Spot.global.presentation.advice.DuplicateResourceException;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUserUpdateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    @Value("${service.active-regions}")
    private List<String> activeRegions;

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    
    // 1. 매장 생성
    @Transactional
    public UUID createStore(StoreCreateRequest dto, Integer userId) {
        
        // 1.0 중복 체크: 도로명주소 + 상세주소 + 매장명( soft delete 제외)
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

        // 1.3 DTO에 담겨온 ID로 유저를 찾아서 스태프 등록
        UserEntity owner = userRepository.findById(dto.ownerId())
                .orElseThrow(() -> new EntityNotFoundException("오너를 찾을 수 없습니다: " + dto.ownerId()));
        UserEntity chef = userRepository.findById(dto.chefId())
                .orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다: " + dto.chefId()));

        store.addStoreUser(owner);
        store.addStoreUser(chef);

        return storeRepository.save(store).getId();
    }

    // 2. 매장 상세 조회
    public StoreDetailResponse getStoreDetails(UUID storeId, Integer userId) {

        // 2.1 유저 조회 및 권한 확인 (인증되지 않은 사용자는 null)
        boolean isAdmin = false;
        if (userId != null) {
            UserEntity currentUser = getValidatedUser(userId);
            isAdmin = checkIsAdmin(currentUser);
        }

        // 2.2 레포지토리 호출
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        // 2.3 서비스 가능 지역인지 검증
        if (!isAdmin) {
            validateServiceRegion(store.getRoadAddress());
        }

        // 2.4 메뉴 목록 조회
        List<MenuEntity> menuEntities = menuRepository.findAllActiveMenus(storeId);
        List<MenuPublicResponseDto> menus = menuEntities.stream()
                .map(menu -> MenuPublicResponseDto.of(menu, menu.getOptions()))
                .toList();

        // 2.5 Entity를 DTO(Response)로 변환하여 반환
        return StoreDetailResponse.fromEntity(store, menus);
    }

    // 3. 매장 전체 조회
    public Page<StoreListResponse> getAllStores(Integer userId, Pageable pageable) {
        // 3.1 사용자의 권한 확인 (인증되지 않은 사용자는 null)
        boolean isAdmin = false;
        if (userId != null) {
            UserEntity currentUser = getValidatedUser(userId);
            isAdmin = checkIsAdmin(currentUser);
        }

        // 3.2 레포지토리 호출 (관리자는 삭제된 것 포함)
        Page<StoreEntity> stores = storeRepository.findAllByRole(isAdmin, pageable);

        // 3.3 서비스지역 기반 필터링 페이지네이션
        return convertToPageResponse(stores, isAdmin, pageable);

    }

    // 4. 매장 기본 정보 수정
    @Transactional
    public void updateStore(UUID storeId, StoreUpdateRequest request, Integer userId) {
        // 4.1 [공통 로직] 조회 + 관리자 스위치 + 소유권 검증
        UserEntity currentUser = getValidatedUser(userId);
        StoreEntity store = findStoreWithAuthority(storeId, currentUser);
        
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
    public void updateStoreStaff(UUID storeId, StoreUserUpdateRequest request, Integer userId) {
        UserEntity currentUser = getValidatedUser(userId);
        StoreEntity store = findStoreWithAuthority(storeId, currentUser);
        
        for (StoreUserUpdateRequest.UserChange change : request.changes()) {
            UserEntity targetUser = userRepository.findById(change.userId())
                    .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 . 없습니다: " + change.userId()));
            
            if (change.action() == StoreUserUpdateRequest.Action.ADD) {
                // 중복 체크
                boolean alreadyStaff = store.getUsers().stream()
                        .anyMatch(su -> su.getUser().getId().equals(change.userId()));
                
                if (!alreadyStaff) {
                    // targetUser가 이미 내부에 자신의 Role을 가지고 있으므로 그대로 등록
                    store.addStoreUser(targetUser);
                }
            } else if (change.action() == StoreUserUpdateRequest.Action.REMOVE) {
                store.getUsers().removeIf(su -> su.getUser().getId().equals(change.userId()));
            }
        }
    }

    // 6. 매장 삭제
    @Transactional
    public void deleteStore(UUID storeId, Integer userId) {
        // 6.1 사용자 조회
        UserEntity currentUser = getValidatedUser(userId);
        boolean isAdmin = checkIsAdmin(currentUser);

        // 6.2 가게 조회 (OWNER는 자신의 모든 상태 가게 삭제 가능)
        StoreEntity store;
        if (isAdmin) {
            // 관리자는 모든 가게 조회 가능
            store = storeRepository.findByIdWithDetails(storeId, true)
                    .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));
        } else {
            // OWNER는 모든 상태(PENDING, APPROVED, REJECTED)의 자신의 가게 조회 가능
            store = storeRepository.findByIdWithDetailsForOwner(storeId)
                    .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

            // OWNER 본인 확인
            boolean isRealOwner = store.getUsers().stream()
                    .anyMatch(su -> su.getUser().getId().equals(currentUser.getId())
                            && su.getUser().getRole() == Role.OWNER);

            if (!isRealOwner) {
                throw new AccessDeniedException("해당 매장에 대한 관리 권한이 없습니다.");
            }
        }

        // 6.3 소프트 삭제
        store.softDelete(userId);
    }
    
    // 7. 매장 이름으로 검색
    public Page<StoreListResponse> searchStoresByName(String keyword, Integer userId, Pageable pageable) {
        // 7.1 사용자의 권한 확인 (인증되지 않은 사용자는 null)
        boolean isAdmin = false;
        if (userId != null) {
            UserEntity currentUser = getValidatedUser(userId);
            isAdmin = checkIsAdmin(currentUser);
        }

        // 7.2 레포지토리 호출
        Page<StoreEntity> stores = storeRepository.searchByName(keyword, isAdmin, pageable);

        // 7.3 서비스지역 기반 필터링 페이지네이션
        return convertToPageResponse(stores, isAdmin, pageable);
    }

    // 8. 내 가게 목록 조회 (OWNER, CHEF)
    public List<StoreListResponse> getMyStores(Integer userId) {
        UserEntity currentUser = getValidatedUser(userId);

        // OWNER나 CHEF만 자신의 가게를 조회할 수 있음
        if (currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.CHEF) {
            throw new AccessDeniedException("OWNER 또는 CHEF만 가게를 조회할 수 있습니다.");
        }

        List<StoreEntity> stores = storeRepository.findAllByOwnerId(userId);

        return stores.stream()
                .map(StoreListResponse::fromEntity)
                .toList();
    }

    // 9. 가게 승인 상태 변경 (MANAGER, MASTER만 가능)
    @Transactional
    public void updateStoreStatus(UUID storeId, com.example.Spot.store.domain.StoreStatus status, Integer userId) {
        UserEntity currentUser = getValidatedUser(userId);

        // 관리자 권한 체크
        if (!checkIsAdmin(currentUser)) {
            throw new AccessDeniedException("관리자만 가게 승인 상태를 변경할 수 있습니다.");
        }

        // 가게 조회 (관리자는 삭제된 가게도 조회 가능)
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, true)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        store.updateStatus(status);
    }
    
    // ----- [공통 검증 로직] -----
    // 1. 현재 유저가 관리자급(MANAGER, MASTER)인지 확인하는 메서드
    private boolean checkIsAdmin(UserEntity user) {
        return user.getRole() == Role.MASTER || user.getRole() == Role.MANAGER;
    }
    
    // 2. 매장을 조회하고 상세 권한을 통합 검증
    private StoreEntity findStoreWithAuthority(UUID storeId, UserEntity currentUser) {
        boolean isAdmin = checkIsAdmin(currentUser);

        // 레포지토리를 이용한 매장 조회
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없거나 접근 권한이 없습니다."));

        // 관리자가 아닐 경우에만 '진짜 주인'인지 추가 확인
        if (!isAdmin) {
            boolean isRealOwner = store.getUsers().stream()
                    .anyMatch(su -> su.getUser().getId().equals(currentUser.getId())
                            && su.getUser().getRole() == Role.OWNER);

            if (!isRealOwner) {
                throw new AccessDeniedException("해당 매장에 대한 관리 권한이 없습니다.");
            }
        }
        return store;
    }
    
    // 3. 서비스 가능한 지역 여부 확인
    private boolean isServiceable(String roadAddress) {
        return activeRegions.stream().anyMatch(roadAddress::contains);
    }
    
    // 4. 서비스 지역 검증(예외 발생) - 상세 조회에서 서비스 불가능 지역일 경우 접근 차단 후 에러 메시지 출력
    private void validateServiceRegion(String roadAddress) {
        if (!isServiceable(roadAddress)) {
            throw new AccessDeniedException("현재 픽업 서비스가 제공되지 않는 지역의 매장입니다.");
        }
    }
    
    // 5. userId(Integer)로 UserEntity를 조회하고 권한 검증 준비
    private UserEntity getValidatedUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }
    
    // 6. 서비스지역기반 필터링 공통 메서드
    private Page<StoreListResponse> convertToPageResponse(Page<StoreEntity> stores, boolean isAdmin, Pageable pageable) {
        if (isAdmin) {
            return stores.map(StoreListResponse::fromEntity);
        }
        List<StoreListResponse> filteredContent = stores.getContent().stream()
                .filter(store -> {
                    boolean serviceable = isServiceable(store.getRoadAddress());
                    return serviceable;
                })
                .map(StoreListResponse::fromEntity)
                .toList();

        return new PageImpl<>(filteredContent, pageable, filteredContent.size());
    }
}
