package com.example.Spot.user.presentation.controller;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
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
@RequestMapping("/api/auth")
public class AuthTokenController {
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ApiResponse<TokenPairResponse> refresh(
            @RequestBody AuthTokenDTO.RefreshRequest request
    ) {
        TokenService.ReissueResult r =
                tokenService.reissueByRefresh(request.getRefreshToken());

        TokenPairResponse result =
                new TokenPairResponse(r.accessToken(), r.refreshToken());

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, result);
    }


    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        tokenService.logoutStateless();
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null);
    }


}
