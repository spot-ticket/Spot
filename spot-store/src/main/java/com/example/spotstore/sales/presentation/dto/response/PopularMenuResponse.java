package com.example.spotstore.sales.presentation.dto.response;

import java.util.UUID;

public record PopularMenuResponse(
        UUID menuId,
        String menuName,
        Long orderCount,
        Long totalRevenue
) {
}
