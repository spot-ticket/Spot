package com.example.Spot.user.presentation.controller;

import com.example.Spot.domain.user.dto.UserResponseDTO;
import com.example.Spot.user.application.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final JoinService joinService;

    //get api
    @GetMapping("/users/{username}")
    public UserResponseDTO getUser(@PathVariable String username) {
        return joinService.getUser(username);
    }
}
