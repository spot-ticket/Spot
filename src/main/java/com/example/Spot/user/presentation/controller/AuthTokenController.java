package com.example.Spot.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.TokenService;
import com.example.Spot.user.presentation.dto.request.AuthTokenDTO;
import com.example.Spot.user.presentation.dto.response.TokenPairResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthTokenController {
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody AuthTokenDTO.RefreshRequest request) {
        TokenService.ReissueResult r = tokenService.reissueByRefresh(request.getRefreshToken());
        return ResponseEntity.ok(new TokenPairResponse(r.accessToken(), r.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // stateless이면 서버는 할 일이 없음 → 클라이언트가 토큰 삭제
        tokenService.logoutStateless();
        return ResponseEntity.ok().build();
    }

}
