package com.example.Spot.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.TokenService;
import com.example.Spot.user.presentation.dto.request.AuthTokenDTO;
import com.example.Spot.user.presentation.dto.response.TokenPairResponse;
import com.example.Spot.user.presentation.swagger.AuthTokenApi;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthTokenController implements AuthTokenApi {
    private final TokenService tokenService;

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody AuthTokenDTO.RefreshRequest request) {
        TokenService.ReissueResult r = tokenService.reissueByRefresh(request.getRefreshToken());
        return ResponseEntity.ok(new TokenPairResponse(r.accessToken(), r.refreshToken()));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // stateless (이후 로직은 client)
        tokenService.logoutStateless();
        return ResponseEntity.ok().build();
    }

}
