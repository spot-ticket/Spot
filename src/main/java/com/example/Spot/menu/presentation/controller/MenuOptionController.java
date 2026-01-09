package com.example.Spot.menu.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuOptionResponseDto;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/stores/{storeId}/menus/{menuId}/options")
@RequiredArgsConstructor
public class MenuOptionController {

    private final MenuOptionService menuOptionService;

    // 메뉴 옵션 조회
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @GetMapping
    public ApiResponse<List<MenuOptionResponseDto>> getOptions(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID storeId,
            @PathVariable UUID menuId
    ) {
        Role role = principal.getUserEntity().getRole();
        List<MenuOptionResponseDto> data = menuOptionService.getOptions(role, storeId, menuId);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
    }

    // 메뉴 옵션 생성
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PostMapping
    public ApiResponse<CreateMenuOptionResponseDto> createMenuOption(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @RequestBody CreateMenuOptionRequestDto request
    ) {
        UserEntity userEntity = principal.getUserEntity();
        CreateMenuOptionResponseDto data = menuOptionService.createMenuOption(userEntity, storeId, menuId, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
    }

    // 메뉴 옵션 변경
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PatchMapping("/{optionId}")
    public ApiResponse<UpdateMenuOptionResponseDto> updateMenuOption(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @PathVariable UUID optionId,
            @RequestBody UpdateMenuOptionRequestDto request
    ) {
        UserEntity userEntity = principal.getUserEntity();
        UpdateMenuOptionResponseDto data = menuOptionService.updateMenuOption(userEntity, storeId, menuId, optionId, request);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
    }

    // 메뉴 옵션 삭제
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @DeleteMapping("/{optionId}")
    public ApiResponse<String> deleteMenuOption(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @PathVariable UUID optionId
    ) {
        UserEntity userEntity = principal.getUserEntity();

        menuOptionService.deleteMenuOption(userEntity, storeId, menuId, optionId);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, "해당 옵션이 삭제되었습니다.");
    }
}
