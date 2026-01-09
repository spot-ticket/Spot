package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuResponseDto;
import com.example.Spot.user.domain.entity.UserEntity;

public interface MenuService {

    // 통합 메뉴 조회
    @Transactional(readOnly = true)
    List<? extends MenuResponseDto> getMenus(UUID storeId, Integer userId);

    // [통합] 메뉴 상세 조회
    @Transactional(readOnly = true)
    MenuResponseDto getMenuDetail(UUID storeId, UUID menuId, Integer userId);

    // 메뉴 생성
    CreateMenuResponseDto createMenu(UUID storeId, CreateMenuRequestDto request, Integer user);

    // 메뉴 업데이트
    UpdateMenuResponseDto updateMenu(UUID storeId, UUID menuId, UpdateMenuRequestDto request, Integer user);
    void deleteMenu(UUID menuId, UserEntity user);   // 메뉴 삭제
    void hiddenMenu(UUID menuId, UpdateMenuHiddenRequestDto request, UserEntity user);  // 메뉴 숨김
}
