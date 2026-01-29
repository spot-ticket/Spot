package com.example.Spot.internal.dto;

import com.example.Spot.user.domain.entity.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserResponse {

    private Integer id;
    private String email;
    private String name;
    private String role;

    public static InternalUserResponse from(UserEntity user) {
        return InternalUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }
}
