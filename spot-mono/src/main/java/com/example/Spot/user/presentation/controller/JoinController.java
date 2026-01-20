package com.example.Spot.user.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.JoinService;
import com.example.Spot.user.presentation.dto.request.JoinDTO;
import com.example.Spot.user.presentation.swagger.JoinApi;

@RestController
public class JoinController implements JoinApi {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @Override
    @PostMapping("/api/join")
    public void joinProcess(@RequestBody JoinDTO joinDTO) {
        joinService.joinProcess(joinDTO);
    }
}
