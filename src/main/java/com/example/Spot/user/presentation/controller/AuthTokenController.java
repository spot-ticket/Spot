package com.example.Spot.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.infra.auth.jwt.JWTUtil;
import com.example.Spot.user.application.service.TokenService;
import com.example.Spot.user.presentation.dto.request.AuthTokenDTO;
import com.example.Spot.user.presentation.dto.response.TokenPairResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthTokenController {
    private final TokenService tokenService;
    private final JWTUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody AuthTokenDTO.RefreshRequest request) {
        TokenService.RefreshIssueResult r = tokenService.rotateRefresh(request.getRefreshToken());
        String newAccess = jwtUtil.createJwt(r.username(), r.role(), 60 * 30L);
        return ResponseEntity.ok(new TokenPairResponse(newAccess, r.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody AuthTokenDTO.LogoutRequest request) {
        tokenService.revokeRefresh(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

}
