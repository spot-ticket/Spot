package com.example.Spot.admin.presentation.dto.request;

import com.example.Spot.user.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateRequestDto {
    private Role role;
}
