package com.example.Spot.store.application.service; // 너희 패키지에 맞춰

import com.example.Spot.global.feign.UserClient;
import org.springframework.stereotype.Service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCallService {

    private final UserClient userClient;

    @CircuitBreaker(name = "user_validate_activeUser")
    @Bulkhead(name = "user_validate_activeUser", type = Bulkhead.Type.SEMAPHORE)
    @Retry(name = "user_validate_activeUser")
    public void validateActiveUser(Integer userId) {
        userClient.validate(userId);
    }
}
