package com.example.Spot.store.presentation.dto.response;

import com.example.Spot.store.domain.entity.StoreEntity;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StoreResponse {
    
    private UUID id;
    private String name;
    private String address;
    private String detailAddress;
    private String phoneNumber;
    private LocalTime openTime;
    private LocalTime closeTime;
    private List<String> categories;
    private String ownerName;
    private String chefName;
    
    public static StoreResponse fromEntity(StoreEntity store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .detailAddress(store.getDetailAddress())
                .phoneNumber(store.getPhoneNumber())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .categories(store.getStoreCategoryMaps().stream()
                        .map(sc -> sc.getCategory().getName())
                        .collect(Collectors.toList()))
                .build();
    }
}
