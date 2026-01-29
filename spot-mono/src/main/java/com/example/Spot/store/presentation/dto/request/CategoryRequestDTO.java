package com.example.Spot.store.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequestDTO {

    public record Create(
            @NotBlank String name
    ) {}

    public record Update(
            @NotBlank String name
    ) {}
}
