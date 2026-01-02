package com.example.Spot.user.presentation.controller;

import com.example.Spot.user.application.service.TokenService;
import com.example.Spot.user.presentation.dto.request.LogoutRequest;
import com.example.Spot.user.presentation.dto.request.RefreshRequest;
import com.example.Spot.user.presentation.dto.request.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest req) {
        tokenService.logout(req.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest req) {
        TokenService.TokenPair pair = tokenService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(new TokenResponse(pair.accessToken(), pair.refreshToken()));
    }
}
