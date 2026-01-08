package com.example.Spot.user.presentation.controller;

import java.util.List;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.UserService;
import com.example.Spot.user.presentation.dto.request.UserUpdateRequestDTO;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    
    // 본인 조회
    @PreAuthorize("#userId == authentication.principal or hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDTO> get(@PathVariable Integer userId) {
        UserResponseDTO result = userService.getByUserId(userId);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, result);
    }

    // 수정
    @PreAuthorize("#userId == authentication.principal or hasRole('ADMIN')")
    @PatchMapping("/{userId}")
    public ApiResponse<UserResponseDTO> update(
            @PathVariable Integer userId,
            @RequestBody UserUpdateRequestDTO request
    ) {
        UserResponseDTO result = userService.updateById(userId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, result);
    }

    // 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ApiResponse<Void> delete(Authentication authentication) {
        Integer loginUserId = (Integer) authentication.getPrincipal();
        userService.deleteMe(loginUserId);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null);
    }
    
    // 닉네임으로 사용자 검색
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @GetMapping("/search")
    public ApiResponse<List<UserResponseDTO>> searchUsers(
            @RequestParam String nickname
    ) {
        List<UserResponseDTO> result = userService.searchUsersByNickname(nickname);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, result);
    }
}
