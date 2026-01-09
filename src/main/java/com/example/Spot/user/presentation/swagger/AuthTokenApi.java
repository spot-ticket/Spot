package com.example.Spot.user.presentation.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Spot.user.presentation.dto.request.AuthTokenDTO;
import com.example.Spot.user.presentation.dto.response.TokenPairResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증 토큰", description = "토큰 갱신 및 로그아웃 API")
public interface AuthTokenApi {

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    ResponseEntity<TokenPairResponse> refresh(@RequestBody AuthTokenDTO.RefreshRequest request);

    @Operation(summary = "로그아웃", description = "로그아웃 처리합니다. (Stateless)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    ResponseEntity<Void> logout();
}
