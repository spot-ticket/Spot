package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuOptionResponseDto;
import com.example.Spot.user.domain.Role;

public interface MenuOptionService {
    List<MenuOptionResponseDto> getOptions(Role userRole, UUID storeId, UUID menuId);
    CreateMenuOptionResponseDto createMenuOption(Integer userId, UUID storeId, UUID menuId, CreateMenuOptionRequestDto request);
    UpdateMenuOptionResponseDto updateMenuOption(Integer userId, UUID storeId, UUID menuId, UUID optionId, UpdateMenuOptionRequestDto request);
    void deleteMenuOption(Integer userId, UUID storeId, UUID menuId, UUID optionId);
}
