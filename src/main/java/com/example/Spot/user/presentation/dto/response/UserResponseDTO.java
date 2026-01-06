package com.example.Spot.user.presentation.dto.response;

import com.example.Spot.user.domain.Role;

public record UserResponseDTO(
        int id,
        String username,
        Role role,

        String nickname,
        String email,
        String roadAddress,
        String addressDetail,
        Integer age,
        Boolean male
) {

}
