package com.example.Spot.user.presentation.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.infrastructure.config.security.CustomUserDetails;

@RestController
public class AuthController {

    @GetMapping("/api/auth/me")
    public Map<String, Object> me(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return Map.of(
                    "userId", customUserDetails.getUserId(),
                    "role", customUserDetails.getRole().name()
            );
        }

        return Map.of(
                "userId", null,
                "role", null
        );
    }
}
