package com.example.Spot.store.application.service;

import org.springframework.stereotype.Service;

import com.example.Spot.global.feign.UserClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCallService {

    private final UserClient userClient;

    @CircuitBreaker(name = "user_validate_activeUser")
    @Retry(name = "user_validate_activeUser")
    public void validateActiveUser(Integer userId) {
        userClient.validate(userId);
    }
}
