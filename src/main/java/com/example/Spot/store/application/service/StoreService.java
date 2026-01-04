package com.example.Spot.store.application.service;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.repository.UserRepository;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    // 1. 매장 생성
    @Transactional
    public UUID createStore(StoreCreateRequest dto, UserEntity currentUser){

        // 1.1 권한 검증: 권한이 셰프와 유저일 경우 예외처리
        if (currentUser.getRole() == UserRole.USER || currentUser.getRole() == UserRole.CHEF) {
            throw new AccessDeniedException("매장 생성 권한이 없습니다.");
        }
        // 1.2 매장 엔티티 생성(DTO -> Entity)
        StoreEntity store = dto.toEntity();

        // 1.3 카테고리 연결
        List<CategoryEntity> categories = categoryRepository.findAllById(dto.getCategoryIds());
        categories.forEach(store::addCategory);

        // 1.4 DTO에 담겨온 ID로 유저를 찾아서 스태프 등록
        UserEntity owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("오너를 찾을 수 없습니다."));
        UserEntity chef = userRepository.findById(dto.getChefId())
                .orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다."));

        store.addStoreUser(owner);
        store.addStoreUser(chef);

        return storeRepository.save(store).getId();
    }

    // 2. 매장 상세 조회
    public StoreResponse getStoreDetails(UUID storeId, UserEntity currentUser) {

        // 2.1 현재 사용자의 권한 확인(True/False)
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN ||
                currentUser.getRole() == UserRole.MANAGER;

        // 2.2 레포지토리 호출
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없거나 접근 권한이 없습니다."));
        
        // 2.3 서비스 가능 지역인지 검증
        if (!isAdmin) {
            boolean isServiceable = activeRegions.stream()
                    .anyMatch(region -> store.getAddress().contains(region));
            
            if (!isServiceable) {
                throw new AccessDeniedException("선택하신 매장은 현재 픽업 서비스가 제공되지 않는 지역에 위치하고 있습니다.");
            }
        }
        
        // 2.4 Entity를 DTO(Response)로 변환하여 반환
        return StoreReponse.fromEntity(store);
    }

    // 3. 매장 전체 조회
    public List<StoreListResponse> getAllStores(UserEntity currentUser) {
        // 3.1 사용자의 권한 확인
        boolean isAdmin = (currentUser.getRole() == UserRole.ADMIN ||
                currentUser.getRole() == UserRole.MANAGER) ;

        // 3.2 레포지토리 호출
        List<StoreEntity> stores = storeRepository.findAllByRole(isAdmin);

        // 3.3 엔티티 리스트 스트림을 사용해 DTO 리스트로 변환하여 반환
        return stores.stream()
                // 일반 유저라면 activeRegions에 포함된 매장만 필터링하여 반환
                .filter(store -> isAdmin || activeRegions.stream().anyMatch(region -> store.getAddress().contains(region)))
                .map(StoreListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 4. 매장 정보 수정
    public void updateStore(UUID storeId, StoreUpdateRequest request, UserEntity currentUser) {
        // 4.1 매장 . 연관 데이터 조회 
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN ||
                currentUser.getRole() == UserRole.ADMIN;

        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        // 4.2 권한 검증
        boolean isOwner = store.getUsers().stream()
                .anyMatch(su -> su.getUser().getId().equals(currentUser.getId()));

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("해당 매장에 대한 수정 권한이 없습니다.");
        }

        // 4.3 기본 정보 업데이트 (Dirty Checking 활용)
        store.updateStoreDetails(
                request.getName(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getOpenTime(),
                request.getCloseTime()
        );

        // 4.4 카테고리 교체 
        if (request.getCategoryIds() != null) {
            store.getStoreCategoryMaps().clear();

            for (UUID categoryId : request.getCategoryIds()) {
                CategoryEntity category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));
                store.addCategory(category);
            }
        }

        // 4.5 스태프(셰프/오너) 교체 
        if (request.getStaffUserIds() != null) {
            store.getUsers().clear();
            for (UUID userId : request.getStaffUserIds()) {
                UserEntity staff = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
                store.addStoreUser(staff);
            }
        }
    }

    // 5. 매장 삭제
    @Transactional
    public void deletedStore(UUID storeId, UserEntity currentUser) {
        // 5.1 매장 조회 (이미 삭제된 것은 제외하고 조회)
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, false)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없거나 이미 삭제되었습니다."));

        // 5.2 권한 검증 (ADMIN, MANAGER, OWNER)
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN ||
                currentUser.getRole() == UserRole.MANAGER;

        boolean isOwner = store.getUsers().stream()
                .anyMatch(su -> su.getUser().getId().equals(currentUser.getId()));

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 5.3 공통 메서드 호출
        store.softDelete();
    }
}
