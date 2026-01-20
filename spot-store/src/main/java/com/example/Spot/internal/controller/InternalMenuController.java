package com.example.Spot.internal.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.internal.dto.InternalMenuOptionResponse;
import com.example.Spot.internal.dto.InternalMenuResponse;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalMenuController {

    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    @GetMapping("/menus/{menuId}")
    public ResponseEntity<InternalMenuResponse> getMenuById(@PathVariable UUID menuId) {
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        return ResponseEntity.ok(InternalMenuResponse.from(menu));
    }

    @GetMapping("/menus/{menuId}/exists")
    public ResponseEntity<Boolean> existsMenuById(@PathVariable UUID menuId) {
        return ResponseEntity.ok(menuRepository.existsById(menuId));
    }

    @GetMapping("/menu-options/{menuOptionId}")
    public ResponseEntity<InternalMenuOptionResponse> getMenuOptionById(@PathVariable UUID menuOptionId) {
        MenuOptionEntity menuOption = menuOptionRepository.findById(menuOptionId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴 옵션을 찾을 수 없습니다."));

        return ResponseEntity.ok(InternalMenuOptionResponse.from(menuOption));
    }
}
