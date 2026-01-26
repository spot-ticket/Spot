package com.example.Spot.user.application.service;

import org.springframework.stereotype.Service;

import com.example.Spot.user.presentation.dto.request.LoginDTO;
import com.example.Spot.user.presentation.dto.response.LoginResponseDTO;

@Service
public class LoginServiceImpl implements LoginService {

    @Override
    public LoginResponseDTO loginProcess(LoginDTO loginDTO) {
        return new LoginResponseDTO(null, null, null, null, null);
    }
}