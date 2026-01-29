package com.example.Spot.global.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.Spot.global.feign.dto.UserResponse;

@FeignClient(name = "spot-user", url = "${feign.user.url}")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUser(@PathVariable("userId") Integer userId);

    // 계정 상태/접근 가능 여부만 확인
    @GetMapping("/api/internal/users/{userId}/validate")
    void validate(@PathVariable("userId") Integer userId);

}
