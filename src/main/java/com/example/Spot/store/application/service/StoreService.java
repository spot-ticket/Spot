package com.example.Spot.store.application.service;

import com.example.Spot.user.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    
    // 1. 역할에 따른 매장 목록 조회
    public List<StoreEntity> getStores(boolean includeDeleted){
        return includeDeleted
                ? storeRepository.findAll()
                : storeRepository.findByIsDeletedFalse ();
    }
    
    // 2. 매장 상세 조회
    public StoreEntity getStoreById(UUID id, boolean includeDeleted){
        // 2.1. 조건에 따라 호출할 레포지토리 메서드만 선택
        Optional<StoreEntity> store = includeDeleted
                ? storeRepository.findById(id)
                : storeRepository.findByIdAndIsDeletedFalse(id);
        // 2.2. 결과 처리 (공통)
        return store.orElseThrow(() -> new RuntimeException("매장을 찾을 수 없습니다."));
    }
    
    // 3. 매장 생성
    @Transactional
    public StoreCreateResponseDto createStore(StoreCreateRequestDto dto){
        // 3.1 변환 및 엔티티 생성(DTO가 담당하거나 서비스가 간단히 처리)
        StoreEntity store = dto.toEntity();
        
        // 3.2 비즈니스 규칙 실행 
        store.addStoreUser(dto.getOwner_id(), "OWNER");
        store.addStoreUser(dto.getChef_id(), "CHEF");
        
        // 3.3 저장
        StoreEntity savedStore = storeRepository.save(store);
        
        // 3.4 결과 반환(DTO)
        return StoreCreateResponseDto.from(savedStore, dto.getOwner_id(), dto.getChef());
    } 
    
}
