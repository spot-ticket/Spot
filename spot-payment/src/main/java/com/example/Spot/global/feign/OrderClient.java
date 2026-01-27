package com.example.Spot.global.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.Spot.global.feign.dto.OrderResponse;

@FeignClient(name = "spot-order", url = "${feign.order.url}")
public interface OrderClient {

    @GetMapping("/api/internal/orders/{orderId}")
    OrderResponse getOrderById(@PathVariable("orderId") UUID orderId);

    @GetMapping("/api/internal/orders/{orderId}/exists")
    boolean existsById(@PathVariable("orderId") UUID orderId);
}
