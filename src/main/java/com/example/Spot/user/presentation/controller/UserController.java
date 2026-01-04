package com.example.Spot.user.presentation.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.UserService;
import com.example.Spot.user.presentation.dto.request.UserUpdateRequestDTO;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @GetMapping("/{username}")
    public UserResponseDTO get(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @PatchMapping("/{username}")
    public UserResponseDTO update(
            @PathVariable String username,
            @RequestBody UserUpdateRequestDTO request
    ) {
        return userService.updateByUsername(username, request);
    }

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
