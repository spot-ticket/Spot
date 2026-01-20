package com.example.spotstore.sales.presentation.dto.response;

import java.time.LocalDate;

public record DailySalesResponse(
        LocalDate date,
        Long revenue,
        Long orderCount
) {
}
