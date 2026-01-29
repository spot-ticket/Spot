package com.example.Spot.global.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.Spot.global.feign.dto.MenuOptionResponse;
import com.example.Spot.global.feign.dto.MenuResponse;

@FeignClient(name = "spot-menu", url = "${feign.store.url}")
public interface MenuClient {

    @GetMapping("/api/internal/menus/{menuId}")
    MenuResponse getMenuById(@PathVariable("menuId") UUID menuId);

    @GetMapping("/api/internal/menu-options/{menuOptionId}")
    MenuOptionResponse getMenuOptionById(@PathVariable("menuOptionId") UUID menuOptionId);

    @GetMapping("/api/internal/menus/{menuId}/exists")
    boolean existsMenuById(@PathVariable("menuId") UUID menuId);
}
