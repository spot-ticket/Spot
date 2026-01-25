//package com.example.Spot.user.presentation.controller;
//
//import java.util.Map;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.Spot.global.infrastructure.config.security.CustomUserDetails;
//import com.example.Spot.user.application.service.JoinService;
//import com.example.Spot.user.presentation.dto.request.JoinDTO;
//import com.example.Spot.user.presentation.swagger.JoinApi;
//
//
//@RestController
//public class JoinController implements JoinApi {
//
//    private final JoinService joinService;
//
//    public JoinController(JoinService joinService) {
//        this.joinService = joinService;
//    }
//
//    @Override
//    @PostMapping("/api/join")
//    public void joinProcess(@RequestBody JoinDTO joinDTO) {
//        joinService.joinProcess(joinDTO);
//    }
//
//    @GetMapping("/api/auth/me")
//    public Map<String, Object> me(Authentication authentication) {
//        CustomUserDetails p = (CustomUserDetails) authentication.getPrincipal();
//        return Map.of(
//                "userId", p.getUserId(),
//                "role", p.getRole().name()
//        );
//    }
//
//}
