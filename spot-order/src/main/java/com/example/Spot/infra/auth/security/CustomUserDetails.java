package com.example.Spot.infra.auth.security;

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
}
