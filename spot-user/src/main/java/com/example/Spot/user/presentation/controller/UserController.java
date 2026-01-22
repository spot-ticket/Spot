package com.example.Spot.user.presentation.controller;

import java.util.List;

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
import com.example.Spot.user.presentation.swagger.UserApi;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UserService userService;
    
    @Override
    @PreAuthorize("#userId == authentication.principal.userId or hasAnyRole('MASTER','MANAGER')")
    @GetMapping("/{userId}")
    public UserResponseDTO get(@PathVariable Integer userId) {
        return userService.getByUserId(userId);
    }

    @Override
    @PreAuthorize("#userId == authentication.principal.userId or hasAnyRole('MASTER','MANAGER')")
    @PatchMapping("/{userId}")
    public UserResponseDTO update(
            @PathVariable Integer userId,
            @RequestBody UserUpdateRequestDTO request
    ) {
        return userService.updateById(userId, request);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public void delete(Authentication authentication) {
        Integer loginUserId = (Integer) authentication.getPrincipal();
        userService.deleteMe(loginUserId);
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(userService.searchUsersByNickname(nickname));
    }
}
