package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.common.Role;
import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;

public interface MenuService {

    @Transactional(readOnly = true)
    List<? extends MenuResponseDto> getMenus(UUID storeId, Integer userId, Role userRole);

    @Transactional(readOnly = true)
    MenuResponseDto getMenuDetail(UUID storeId, UUID menuId, Integer userId, Role userRole);

    CreateMenuResponseDto createMenu(UUID storeId, CreateMenuRequestDto request, Integer userId, Role userRole);

    MenuAdminResponseDto updateMenu(UUID storeId, UUID menuId, UpdateMenuRequestDto request, Integer userId, Role userRole);

    void deleteMenu(UUID menuId, Integer userId, Role userRole);

    void hiddenMenu(UUID menuId, UpdateMenuHiddenRequestDto request, Integer userId, Role userRole);
}
