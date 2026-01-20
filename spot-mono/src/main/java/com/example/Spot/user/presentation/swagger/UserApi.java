package com.example.Spot.user.presentation.swagger;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.user.presentation.dto.request.UserUpdateRequestDTO;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자", description = "사용자 관리 API")
public interface UserApi {

    @Operation(summary = "사용자 조회", description = "특정 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    UserResponseDTO get(
            @Parameter(description = "사용자 ID") @PathVariable Integer userId);

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    UserResponseDTO update(
            @Parameter(description = "사용자 ID") @PathVariable Integer userId,
            @RequestBody UserUpdateRequestDTO request);

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    void delete(Authentication authentication);

    @Operation(summary = "사용자 검색", description = "닉네임으로 사용자를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<List<UserResponseDTO>> searchUsers(
            @Parameter(description = "검색할 닉네임") @RequestParam String nickname);
}
