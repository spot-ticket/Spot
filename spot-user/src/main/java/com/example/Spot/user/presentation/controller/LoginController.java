package com.example.Spot.user.presentation.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.infrastructure.config.security.CustomUserDetails;
import com.example.Spot.user.application.service.LoginService;
import com.example.Spot.user.presentation.dto.request.LoginDTO;
import com.example.Spot.user.presentation.dto.response.LoginResponseDTO;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/api/login")
    public LoginResponseDTO login(@RequestBody LoginDTO loginDTO) {
        return loginService.loginProcess(loginDTO);
    }

    @GetMapping("/api/auth/me")
    public Map<String, Object> me(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return Map.of(
                "userId", principal.getUserId(),
                "role", principal.getRole().name()
        );
    }
}
