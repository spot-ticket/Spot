package com.example.Spot.menu.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.example.Spot.menu.application.service.MenuService;
import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuResponseDto;
import com.example.Spot.menu.presentation.swagger.MenuApi;
import com.example.Spot.user.domain.Role;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController implements MenuApi {

    private final MenuService menuService;

    @Override
    @GetMapping
    public ApiResponse<List<? extends MenuResponseDto>> getMenus(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal Integer userId
    ) {
        // 비로그인 유저 또는 로그인 유저의 Role 확인
        Role role = getCurrentRole();

        // 유저 권한에 따른 접근 처리
        if (role == Role.CUSTOMER) {
            List<MenuPublicResponseDto> data = menuService.getMenusForCustomer(storeId);
            return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
        } else {
            List<MenuAdminResponseDto> data = menuService.getMenusForAdmin(storeId, role);
            return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
        }
    }

    @Override
    @GetMapping("/{menuId}")
    public ApiResponse<MenuPublicResponseDto> getMenuDetail(@PathVariable UUID menuId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, menuService.getMenuDetail(menuId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PostMapping
    public ApiResponse<CreateMenuResponseDto> createMenu(
            @PathVariable UUID storeId,
            @RequestBody CreateMenuRequestDto request,
            @AuthenticationPrincipal Integer userId
    ) {
        CreateMenuResponseDto data = menuService.createMenu(storeId, request, userId);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PatchMapping("/{menuId}")
    public ApiResponse<UpdateMenuResponseDto> updateMenu(
            @PathVariable UUID menuId,
            @RequestBody UpdateMenuRequestDto request,
            @AuthenticationPrincipal Integer userId
    ) {
        UpdateMenuResponseDto data = menuService.updateMenu(menuId, request, userId);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, data);

    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @DeleteMapping("/{menuId}")
    public ApiResponse<String> deleteMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal Integer userId
    ) {
        menuService.deleteMenu(menuId, userId);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, "메뉴가 삭제되었습니다.");
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER', 'OWNER')")
    @PatchMapping("/{menuId}/hide")
    public ApiResponse<String> hiddenMenu(
            @PathVariable UUID menuId,
            @RequestBody UpdateMenuHiddenRequestDto request,
            @AuthenticationPrincipal Integer userId
    ) {
        menuService.hiddenMenu(menuId, request, userId);

        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, "해당 메뉴를 숨김 처리하였습니다.");
    }

    // Helper - SecurityContextHolder에서 Role 가져오기
    private Role getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return Role.CUSTOMER; // 비로그인 사용자는 CUSTOMER로 처리
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> Role.valueOf(auth.substring(5)))
                .findFirst()
                .orElse(Role.CUSTOMER);
    }
}
