package com.example.Spot.store.presentation.dto.response;

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
}
