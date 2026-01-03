package com.example.Spot.user.presentation.controller;

import com.example.Spot.user.application.service.TokenService;
import com.example.Spot.user.presentation.dto.request.AuthTokenDTO;
import com.example.Spot.user.presentation.dto.response.TokenPairResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthTokenController {

    private final TokenService tokenService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody AuthTokenDTO.LogoutRequest request) {
        tokenService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(
            @RequestBody AuthTokenDTO.RefreshRequest request) {
        TokenService.TokenPair pair = tokenService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(new TokenPairResponse(pair.accessToken(), pair.refreshToken()));
    }

}
