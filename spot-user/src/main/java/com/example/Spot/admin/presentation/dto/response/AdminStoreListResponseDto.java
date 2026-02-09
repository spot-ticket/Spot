package com.example.Spot.admin.presentation.dto.response;

import java.util.UUID;

public record AdminStoreListResponseDto(
        UUID id,
        String name,
        String roadAddress,
        String addressDetail,
        String phoneNumber,
        String status,
        boolean isDeleted
) {}
