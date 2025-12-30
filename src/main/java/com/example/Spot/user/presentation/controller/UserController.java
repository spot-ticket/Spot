package com.example.Spot.user.presentation.controller;

import com.example.Spot.user.presentation.dto.request.UserResponseDTO;
import com.example.Spot.user.application.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
