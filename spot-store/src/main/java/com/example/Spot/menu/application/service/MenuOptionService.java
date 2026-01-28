package com.example.Spot.menu.application.service;

import java.util.UUID;

import com.example.Spot.global.common.Role;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;

public interface MenuOptionService {

    CreateMenuOptionResponseDto createMenuOption(UUID storeId, UUID menuId, Integer userId, Role userRole, CreateMenuOptionRequestDto request);

    MenuOptionAdminResponseDto updateMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionRequestDto request);

    void deleteMenuOption(UUID storeId, UUID menuId, UUID optionID, Integer userId, Role userRole);

    void hiddenMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionHiddenRequestDto request);
}
