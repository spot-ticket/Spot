package com.example.Spot.admin.application.service;

import java.util.List;
import java.util.UUID;


import com.example.Spot.global.feign.dto.StoreResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import com.example.Spot.admin.presentation.dto.response.AdminStoreListResponseDto;
import com.example.Spot.global.feign.StoreAdminClient;
import com.example.Spot.global.feign.dto.StorePageResponse;


import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private final StoreAdminClient storeAdminClient;


    public StorePageResponse<AdminStoreListResponseDto> getAllStores(Pageable pageable) {
        StorePageResponse<StoreResponse> page = storeAdminClient.getAllStores(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                "name",
                "ASC"
        );

        if (page == null) {
            return new StorePageResponse<>(
                    List.of(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    0L,
                    0,
                    true,
                    true
            );
        }

        List<AdminStoreListResponseDto> mapped = page.content() == null ? List.of()
                : page.content().stream()
                .map(s -> new AdminStoreListResponseDto(
                        s.getId(),
                        s.getName(),
                        s.getRoadAddress(),
                        s.getAddressDetail(),
                        s.getPhoneNumber(),
                        s.getStatus(),
                        s.isDeleted()
                ))
                .toList();

        return new StorePageResponse<>(
                mapped,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last()
        );
    }

    public long getStoreCount() {
        return storeAdminClient.getStoreCount();
    }


    public void approveStore(UUID storeId) {
        storeAdminClient.updateStoreStatus(storeId, "APPROVED");
    }

    public void deleteStore(UUID storeId, Integer userId) {
        storeAdminClient.deleteStore(storeId);
    }


}
