package com.example.Spot.menu.presentation.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.menu.application.service.MenuOptionService;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;
import com.example.Spot.user.domain.Role;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stores/{storeId}/menus/{menuId}/options")
@RequiredArgsConstructor
public class MenuOptionController {

    private final MenuOptionService menuOptionService;

    // 메뉴 옵션 생성
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PostMapping
    public ApiResponse<CreateMenuOptionResponseDto> createMenuOption(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @Valid @RequestBody CreateMenuOptionRequestDto request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        Role role = principal.getUserRole();
        CreateMenuOptionResponseDto data = menuOptionService.createMenuOption(storeId, menuId, userId, role, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
    }

    // 메뉴 옵션 변경
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PatchMapping("/{optionId}")
    public ApiResponse<MenuOptionAdminResponseDto> updateMenuOption(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @PathVariable UUID optionId,
            @Valid @RequestBody UpdateMenuOptionRequestDto request
    ) {
        Integer userId = principal.getUserId();
        Role role = principal.getUserRole();
        MenuOptionAdminResponseDto data = menuOptionService.updateMenuOption(storeId, menuId, optionId, userId, role, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
    }

    // 메뉴 옵션 삭제
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @DeleteMapping("/{optionId}")
    public ApiResponse<String> deleteMenuOption(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @PathVariable UUID optionId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        Role role = principal.getUserRole();
        menuOptionService.deleteMenuOption(storeId, menuId, optionId, userId, role);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, "해당 옵션이 삭제되었습니다.");
    }

    // 메뉴 옵션 숨김
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PatchMapping("/{optionId}/hide")
    public ApiResponse<String> hiddenMenuOption(
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @PathVariable UUID optionId,
            @Valid @RequestBody UpdateMenuOptionHiddenRequestDto request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Integer userId = principal.getUserId();
        Role userRole = principal.getUserRole();
        menuOptionService.hiddenMenuOption(storeId, menuId, optionId, userId, userRole, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, "해당 메뉴의 옵션을 숨김 처리하였습니다.");
    }
}
