package com.example.Spot.domain.user.controller;

import com.example.Spot.domain.user.dto.UserResponseDTO;
import com.example.Spot.domain.login.service.JoinService;
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
