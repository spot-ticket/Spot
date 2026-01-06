package com.example.Spot.store.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
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

    // 1. 매장 생성
    @Transactional
    public UUID createStore(StoreCreateRequest dto, UserEntity currentUser) {
        
        // 1.1 DTO에 넘겨줄 카테고리 리스트를 생성
        List<CategoryEntity> categories = dto.getCategoryNames().stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다.: " + name)))
                .toList();
        
        // 1.2 매장 엔티티 생성(DTO -> Entity)
        StoreEntity store = dto.toEntity(categories);

        // 1.3 DTO에 담겨온 ID로 유저를 찾아서 스태프 등록
        UserEntity owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("오너를 찾을 수 없습니다: " + dto.getOwnerId()));
        UserEntity chef = userRepository.findById(dto.getChefId())
                .orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다: " + dto.getOwnerId()));

        store.addStoreUser(owner);
        store.addStoreUser(chef);

        return storeRepository.save(store).getId();
    }

    // 2. 매장 상세 조회
    public StoreDetailResponse getStoreDetails(UUID storeId, UserEntity currentUser) {

        // 2.1 현재 사용자의 권한 확인(True/False)
        boolean isAdmin = checkIsAdmin(currentUser);

        // 2.2 레포지토리 호출
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));
        
        // 2.3 서비스 가능 지역인지 검증
        if (!isAdmin) {
            validateServiceRegion(store.getAddress());
        }
        
        // 2.4 Entity를 DTO(Response)로 변환하여 반환
        return StoreDetailResponse.fromEntity(store);
    }

    // 3. 매장 전체 조회
    public List<StoreListResponse> getAllStores(UserEntity currentUser) {
        // 3.1 사용자의 권한 확인
        boolean isAdmin = checkIsAdmin(currentUser);

        // 3.2 레포지토리 호출 (관리자는 삭제된 것 포함)
        List<StoreEntity> stores = storeRepository.findAllByRole(isAdmin);

        // 3.3 엔티티 리스트 스트림을 사용해 DTO 리스트로 변환하여 반환
        return stores.stream()
                // 일반 유저라면 activeRegions에 포함된 매장만 필터링하여 반환
                .filter(store -> isAdmin || isServiceable(store.getAddress()))
                .map(StoreListResponse::fromEntity)
                .toList();
    }

    // 4. 매장 정보 수정
    public void updateStore(UUID storeId, StoreUpdateRequest request, UserEntity currentUser) {
        // 4.1 [공통 로직] 조회 + 관리자 스위치 + 소유권 검증 
        StoreEntity store = findStoreWithAuthority(storeId, currentUser);
        
        // 4.2 카테고리 이름 리스트를 엔티티 리스트로 변환
        List<CategoryEntity> categories = null;
        if (request.getCategoryNames() != null) {
            categories = request.getCategoryNames().stream()
                    .map(name -> categoryRepository.findByName(name)
                            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리: " + name)))
                    .toList();
        }
        
        // 4.3 엔티티 내부 메서드 호출
        store.updateStoreDetails(
                request.getName(),
                request.getAddress(),
                request.getDetailAddress(),
                request.getPhoneNumber(),
                request.getOpenTime(),
                request.getCloseTime(),
                categories
        );
    }

    // 5. 매장 삭제
    @Transactional
    public void deleteStore(UUID storeId, UserEntity currentUser) {
        // 5.1 [공통 로직] 조회 및 권한 체크
        StoreEntity store = findStoreWithAuthority(storeId, currentUser);
        
        // 5.2 공통 메서드 호출
        store.softDelete();
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
    private boolean isServiceable(String address) {
        return activeRegions.stream().anyMatch(address::contains);
    }
    
    // 4. 서비스 지역 검증(예외 발생) - 상세 조회에서 서비스 불가능 지역일 경우 접근 차단 후 에러 메시지 출력
    private void validateServiceRegion(String address) {
        if (!isServiceable(address)) {
            throw new AccessDeniedException("현재 픽업 서비스가 제공되지 않는 지역의 매장입니다.");
        }
    }
}
