package com.example.Spot.internal.dto;

import java.util.UUID;

import com.example.Spot.store.domain.entity.StoreUserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalStoreUserResponse {

    private UUID id;
    private UUID storeId;
    private Integer userId;
    private String role;

    public static InternalStoreUserResponse from(StoreUserEntity storeUser) {
        return InternalStoreUserResponse.builder()
                .id(storeUser.getId())
                .storeId(storeUser.getStore().getId())
                .userId(storeUser.getUserId())
                .role(storeUser.getRole().name())
                .build();
    }
}
