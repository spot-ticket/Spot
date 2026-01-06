package com.example.Spot.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.PasswordResetService;
import com.example.Spot.user.application.service.TokenService;
import com.example.Spot.user.presentation.dto.request.AuthTokenDTO;
import com.example.Spot.user.presentation.dto.request.ResetTokenDTO;
import com.example.Spot.user.presentation.dto.response.TokenPairResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthTokenController {

    private final TokenService tokenService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody AuthTokenDTO.RefreshRequest request) {
        TokenService.ReissueResult r = tokenService.reissueByRefresh(request.getRefreshToken());
        return ResponseEntity.ok(new TokenPairResponse(r.accessToken(), r.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // stateless (이후 로직은 client)
        tokenService.logoutStateless();
        return ResponseEntity.ok().build();
    }



    // 패스워드 리셋
    @PostMapping("/reset-password-request")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody ResetTokenDTO.EmailRequest request) {
        passwordResetService.sendResetLink(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetTokenDTO.ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
