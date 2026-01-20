package com.example.Spot.store.presentation.dto.response;

import java.time.LocalTime;
import java.util.UUID;

public class CategoryResponseDTO {

    public record CategoryItem(
            UUID id,
            String name
    ) {}

    public record CategoryDetail(
            UUID id,
            String name
    ) {}

    public record StoreSummary(
            UUID id,
            String name,
            String roadAddress,
            String phoneNumber,
            LocalTime openTime,
            LocalTime closeTime
    ) {}
}
