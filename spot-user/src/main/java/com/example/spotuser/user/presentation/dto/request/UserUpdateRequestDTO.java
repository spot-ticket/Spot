package com.example.spotuser.user.presentation.dto.request;

public record UserUpdateRequestDTO(
        String nickname,
        String email,
        String roadAddress,
        String addressDetail,
        Integer age,
        Boolean male
) {}
