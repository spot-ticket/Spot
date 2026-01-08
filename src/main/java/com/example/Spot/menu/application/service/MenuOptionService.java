package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

public interface MenuOptionService {
    // 메뉴 옵션
    List<MenuOptionAdminResponseDto> getOptions(Role userRole, UUID storeId, UUID menuId);

    MenuOptionAdminResponseDto createMenuOption(UserEntity user, UUID storeId, UUID menuId, CreateMenuOptionRequestDto request);

    MenuOptionAdminResponseDto updateMenuOption(UserEntity user, UUID storeId, UUID menuId, UUID optionId, UpdateMenuOptionRequestDto request);

    void deleteMenuOption(UserEntity user, UUID storeId, UUID menuId, UUID optionId);
}
