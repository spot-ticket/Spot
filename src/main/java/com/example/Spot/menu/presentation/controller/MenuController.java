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
import com.example.Spot.menu.application.service.MenuService;
import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuResponseDto;
import com.example.Spot.user.domain.Role;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 메뉴 전체 조회
    @GetMapping
    public ApiResponse<List<? extends MenuResponseDto>> getMenus(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 비로그인 유저
        Role role = (user != null) ? user.getUserEntity().getRole() : Role.CUSTOMER;

        // 유저 권한에 따른 접근 처리
        if (role == Role.CUSTOMER) {
            List<MenuPublicResponseDto> data = menuService.getMenusForCustomer(storeId);
            return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
        } else {
            List<MenuAdminResponseDto> data = menuService.getMenusForAdmin(storeId, role);
            return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
        }
    }

    // 메뉴 상세 조회
    @GetMapping("/{menuId}")
    public ApiResponse<MenuPublicResponseDto> getMenuDetail(@PathVariable UUID menuId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, menuService.getMenuDetail(menuId));
    }

    // 메뉴 생성
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PostMapping
    public ApiResponse<CreateMenuResponseDto> createMenu(
            @PathVariable UUID storeId,
            @RequestBody CreateMenuRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CreateMenuResponseDto data = menuService.createMenu(storeId, request, user.getUserEntity());

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
    }

    // 메뉴 변경
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PatchMapping("/{menuId}")
    public ApiResponse<UpdateMenuResponseDto> updateMenu(
            @PathVariable UUID menuId,
            @RequestBody UpdateMenuRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateMenuResponseDto data = menuService.updateMenu(menuId, request, user.getUserEntity());

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);

    }

    // 메뉴 삭제
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @DeleteMapping("/{menuId}")
    public ApiResponse<String> deleteMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        menuService.deleteMenu(menuId, user.getUserEntity());

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, "메뉴가 삭제되었습니다.");
    }

    // 메뉴 숨김
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PatchMapping("/{menuId}/hide")
    public ApiResponse<String> hiddenMenu(
            @PathVariable UUID menuId,
            @RequestBody UpdateMenuHiddenRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        menuService.hiddenMenu(menuId, request, user.getUserEntity());

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, "해당 메뉴를 숨김 처리하였습니다.");
    }
}
