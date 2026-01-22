package com.example.Spot.infra.auth.security;

import com.example.Spot.global.common.Role;

public class CustomUserDetails {

    private final Integer userId;
    private final String role; // "OWNER", "CHEF", "MASTER", "MANAGER"

    public CustomUserDetails(Integer userId, String role) {
        this.userId = userId;
        this.role = role;
    }
    public static CustomUserDetails forDev(Integer userId, String role) {
        return new CustomUserDetails(userId, role);
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public Role getUserRole() {
        return Role.valueOf(role);
    }
}
