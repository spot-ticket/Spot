package com.example.Spot.user.presentation.controller;

import com.example.Spot.user.application.service.UserService;
import com.example.Spot.user.presentation.dto.request.UserUpdateRequestDTO;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 조회
    @GetMapping("/{username}")
    public UserResponseDTO get(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    // 수정
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @PatchMapping("/{username}")
    public UserResponseDTO update(
            @PathVariable String username,
            @RequestBody UserUpdateRequestDTO request
    ) {
        return userService.updateByUsername(username, request);
    }

    // 삭제(탈퇴/soft delete)
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public void delete(
            @PathVariable String username,
            Authentication authentication
    ) {
        String loginUsername = authentication.getName();
        userService.deleteByUsername(username, loginUsername);
    }
}
