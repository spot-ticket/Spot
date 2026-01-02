package com.example.Spot.user.presentation.controller.testsupport;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthController {

    @GetMapping("/api/test-auth")
    public String testAuth(Authentication authentication) {
        // 인증이 붙으면 authentication이 null이 아님
        return (authentication != null) ? "ok" : "no-auth";
    }
}
