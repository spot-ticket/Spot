package com.example.Spot.global.feign.dto;

import java.util.List;

public record StorePageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}
