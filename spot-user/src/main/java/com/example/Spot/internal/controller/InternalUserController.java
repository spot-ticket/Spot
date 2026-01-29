package com.example.Spot.internal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.internal.dto.InternalUserResponse;
import com.example.Spot.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<InternalUserResponse> getUserById(@PathVariable Integer userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(InternalUserResponse.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Integer userId) {
        return ResponseEntity.ok(userRepository.existsById(userId));
    }
    @GetMapping("/{userId}/validate")
    public ResponseEntity<Void> validate(@PathVariable Integer userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.<Void>notFound().build();
        }
        return ResponseEntity.<Void>ok().build();
    }

}
