package com.example.Spot.global.feign.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreUserResponse {

    private UUID id;
    private UUID storeId;
    private Integer userId;
    private String role;
}
