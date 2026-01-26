package com.example.Spot.user.application.service;

import com.example.Spot.user.presentation.dto.request.LoginDTO;
import com.example.Spot.user.presentation.dto.response.LoginResponseDTO;

public interface LoginService {

    LoginResponseDTO loginProcess(LoginDTO loginDTO);
}
