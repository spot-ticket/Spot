package com.example.Spot.admin.presentation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.admin.application.service.AdminUserService;
import com.example.Spot.admin.presentation.dto.request.UserRoleUpdateRequestDto;
import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<UserResponseDTO> users = adminUserService.getAllUsers(pageable);

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, users));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Integer userId,
            @RequestBody UserRoleUpdateRequestDto request) {

        adminUserService.updateUserRole(userId, request.getRole());

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId) {
        adminUserService.deleteUser(userId);

        return ResponseEntity
                .ok(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null));
    }
}
