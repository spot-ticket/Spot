package com.example.Spot.store.application.service;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.repository.UserRepository;

import java.util.stream.Collectors;
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
        
        // 2.3 Entity를 DTO(Response)로 변환하여 반환
        return StoreReponse.fromEntity(store);
    }
    
    // 3. 매장 전체 조회
    public List<StoreListResponse> getAllStores(UserEntity currentUser) {
        // 3.1 사용자의 권한 확인
        boolean isAdmin = (currentUser.getRole() == UserRole.ADMIN ||
                currentUser.getRole() == UserRole.MANAGER) ;
        
        // 3.2 레포지토리 호출
        List<StoreEntity> stores = storeRepository.findAllByRole(isAdmin);
        
        // 3.3 엔티티 리스트 스트림을 사용해 DTO 리스트로 변환ㄴ하여 반환
        return stores.stream()
                .map(StoreListResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    
    
    
    
}
