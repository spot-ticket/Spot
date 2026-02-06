package com.example.Spot.store.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.presentation.dto.response.AdminStoreListResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStoreInternalService {

    private final StoreService storeService;
    private final StoreRepository storeRepository;

    public Page<AdminStoreListResponse> getAllStores(Pageable pageable) {
        return storeRepository.findAll(pageable)
                .map(AdminStoreListResponse::from);
    }
    @Transactional
    public void deleteStore(UUID storeId, Integer userId) {
        storeService.deleteStore(storeId, userId, true);
    }

    public long getStoreCount() {
        return storeRepository.count();
    }

    public Map<UUID, String> getStoreNames(List<UUID> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Map.of();
        }

        return storeRepository.findStoreNamesByIds(storeIds).stream()
                .collect(Collectors.toMap(
                        r -> (UUID) r[0],
                        r -> (String) r[1]
                ));
    }

}
