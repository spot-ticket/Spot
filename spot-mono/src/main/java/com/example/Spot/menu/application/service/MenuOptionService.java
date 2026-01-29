package com.example.Spot.menu.application.service;

import java.util.UUID;

import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;
import com.example.Spot.user.domain.Role;

public interface MenuOptionService {

    // 메뉴 옵션 생성
    CreateMenuOptionResponseDto createMenuOption(UUID storeId, UUID menuId, Integer userId, Role userRole, CreateMenuOptionRequestDto request);

    // 메뉴 옵션 업데이트
    MenuOptionAdminResponseDto updateMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionRequestDto request);

    // 메뉴 옵션 삭제
    void deleteMenuOption(UUID storeId, UUID menuId, UUID optionID, Integer userId, Role userRole);

    // 메뉴 옵션 숨김
    void hiddenMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionHiddenRequestDto request);
}
