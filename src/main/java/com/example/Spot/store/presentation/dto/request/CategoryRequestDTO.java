package com.example.Spot.store.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequestDTO {

    public record Create(
            @NotBlank String name
    ) {}

    public record Update(
            @NotBlank String name
    ) {}
}
