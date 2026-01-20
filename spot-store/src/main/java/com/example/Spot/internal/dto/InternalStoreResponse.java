package com.example.Spot.internal.dto;

import java.util.UUID;

import com.example.Spot.store.domain.entity.StoreEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalStoreResponse {

    private UUID id;
    private String name;
    private String roadAddress;
    private String addressDetail;
    private String phoneNumber;
    private String status;
    private Integer ownerId;
    private boolean isDeleted;

    public static InternalStoreResponse from(StoreEntity store) {
        return InternalStoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .roadAddress(store.getRoadAddress())
                .addressDetail(store.getAddressDetail())
                .phoneNumber(store.getPhoneNumber())
                .status(store.getStatus().name())
                .ownerId(store.getOwnerId())
                .isDeleted(store.getIsDeleted())
                .build();
    }
}
