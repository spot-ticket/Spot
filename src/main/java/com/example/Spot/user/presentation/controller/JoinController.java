package com.example.Spot.user.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.user.application.service.JoinService;
import com.example.Spot.user.presentation.dto.request.JoinDTO;


@RestController
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/api/join")
    public void joinProcess(@RequestBody JoinDTO joinDTO) {
        joinService.joinProcess(joinDTO);
    }
}
