package com.example.Spot.sales.presentation.dto.response;

import java.util.UUID;

public record PopularMenuResponse(
        UUID menuId,
        String menuName,
        Long orderCount,
        Long totalRevenue
) {
}
