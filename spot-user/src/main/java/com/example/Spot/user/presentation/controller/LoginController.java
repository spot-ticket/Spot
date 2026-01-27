package com.example.Spot.user.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.LoginService;
import com.example.Spot.user.presentation.dto.request.LoginDTO;
import com.example.Spot.user.presentation.dto.response.LoginResponseDTO;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/api/login")
    public LoginResponseDTO login(@RequestBody LoginDTO loginDTO) {
        return loginService.loginProcess(loginDTO);
    }

}
