package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.presentation.dto.request.*;
import com.example.Spot.menu.presentation.dto.response.*;
import com.example.Spot.user.domain.Role;

public interface MenuOptionService {
    List<MenuOptionResponseDto> getOptions(UUID menuId, Role userRole);
    CreateMenuOptionResponseDto createMenuOption(UUID menuId, CreateMenuOptionRequestDto request);
    UpdateMenuOptionResponseDto updateMenuOption(UUID optionId, UpdateMenuOptionRequestDto request);
    void deleteMenuOption(UUID optionId);
}
