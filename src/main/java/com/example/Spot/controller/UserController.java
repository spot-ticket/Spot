package com.example.Spot.controller;

import com.example.Spot.dto.UserResponseDTO;
import com.example.Spot.service.JoinService;
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
