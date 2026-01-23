package com.example.Spot.admin.presentation.dto;

import java.util.UUID;

public record AdminStoreListResponseDto(
        UUID storeId,
        String name,
        String status
) {}
