package com.example.Spot.user.presentation.dto.request;

public record UserUpdateRequestDTO(
        String nickname,
        String email,
        String address,
        Integer age,
        Boolean male
) {}
