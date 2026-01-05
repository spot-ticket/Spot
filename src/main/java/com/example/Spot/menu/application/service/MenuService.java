package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuResponseDto;
import com.example.Spot.user.domain.Role;

public interface MenuService {
    List<MenuAdminResponseDto> getMenusForAdmin(UUID storeId, Role userRole);  // [관리자, 가게]
    List<MenuPublicResponseDto> getMenusForCustomer(UUID storeId);    // [손님] 메뉴 조회
    MenuPublicResponseDto getMenuDetail(UUID menuId);    // [손님] 메뉴 상세 조회
    CreateMenuResponseDto createMenu(UUID storeId, CreateMenuRequestDto request);   // 메뉴 생성
    UpdateMenuResponseDto updateMenu(UUID menuId, UpdateMenuRequestDto request);    // 메뉴 업데이트
    UUID deleteMenu(UUID menuId);   // 메뉴 삭제
    UpdateMenuResponseDto hiddenMenu(UUID menuId, UpdateMenuHiddenRequestDto request);  // 메뉴 숨김
}
