package com.example.Spot.global.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.global.feign.dto.OrderPageResponse;
import com.example.Spot.global.feign.dto.OrderStatsResponse;

@FeignClient(name = "order-service", url = "${feign.order.url}")
public interface OrderClient {

    @GetMapping("/api/admin/orders")
    OrderPageResponse getAllOrders(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("direction") String direction
    );

    @GetMapping("/api/admin/orders/stats")
    OrderStatsResponse getOrderStats();

    @GetMapping("/api/admin/orders/count")
    long getOrderCount();
}
