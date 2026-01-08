package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

public interface MenuService {
    // [관리자/사장님] 메뉴 목록 조회
    List<MenuAdminResponseDto> getMenusForAdmin(UUID storeId, Role userRole);

    // [손님] 가게 메뉴 목록 조회 (옵션 미포함)
    List<MenuPublicResponseDto> getMenusForCustomer(UUID storeId);

    // [손님] 메뉴 상세 조회 (옵션 포함)
    MenuPublicResponseDto getMenuDetail(UUID menuId);

    // [사장님] 메뉴 삭제
    void deleteMenu(UUID menuId, UserEntity user);
}
