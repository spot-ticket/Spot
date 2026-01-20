package com.example.Spot.global.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.Spot.global.feign.dto.UserResponse;

@FeignClient(name = "user-service", url = "${feign.user.url}")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUser(@PathVariable("userId") Integer userId);

}
