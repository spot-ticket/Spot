package com.example.Spot.admin.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.store.domain.StoreStatus;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStoreService {

    private final StoreRepository storeRepository;

    public Page<StoreListResponse> getAllStores(Pageable pageable) {
        Page<StoreEntity> stores = storeRepository.findAll(pageable);
        return stores.map(StoreListResponse::fromEntity);
    }

    @Transactional
    public void approveStore(UUID storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        // 가게 승인 로직 (필요시 승인 상태 필드 추가)
        store.updateStatus(StoreStatus.APPROVED);
    }

    @Transactional
    public void deleteStore(UUID storeId, Integer userId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        store.softDelete(userId);
    }
}
