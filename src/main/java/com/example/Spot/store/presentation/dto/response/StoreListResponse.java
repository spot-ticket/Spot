package com.example.Spot.store.presentation.dto.response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.store.domain.entity.StoreEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StoreListResponse {
    
    private UUID id;
    private String name;
    private String address;
    private List<String> categories;
    
    // Entity -> DTO 변환 메서드
    public static StoreListResponse fromEntity(StoreEntity store){
        return StoreListResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                // StoreEntity의 중간 매핑 테이블 필드명에 맞춰 호출
                .categories(store.getStoreCategoryMaps().stream()
                        .map(sc -> sc.getCategory().getName())
                        .collect(Collectors.toList()))
                .build();
    }
}
