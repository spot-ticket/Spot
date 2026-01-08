package com.example.Spot.menu.application.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final MenuOptionRepository menuOptionRepository;

    // 1. [관리자/가게] 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuAdminResponseDto> getMenusForAdmin(UUID storeId, Role userRole) {

        List<MenuEntity> menus;

        if (userRole == Role.MANAGER || userRole == Role.MASTER) {
            menus = menuRepository.findAllByStoreId(storeId);
        } else {
            menus = menuRepository.findAllByStoreIdAndIsDeletedFalse(storeId);
        }

        return menus.stream().map(MenuAdminResponseDto::new).toList();
    }

    // 2. [손님] 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuPublicResponseDto> getMenusForCustomer(UUID storeId) {

        List<MenuEntity> menus = menuRepository.findAllActiveMenus(storeId);

        return menus.stream().map(menu -> MenuPublicResponseDto.of(menu, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    // 3. [손님] 특정 메뉴 상세 조회
    @Transactional(readOnly = true)
    public MenuPublicResponseDto getMenuDetail(UUID menuId) {

        MenuEntity menu = menuRepository.findActiveMenuById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        List<MenuOptionEntity> options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId);

        return MenuPublicResponseDto.of(menu, options);
    }

    // 6. 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID menuId, UserEntity user) {

        // 메뉴 조회
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        // 삭제 여부 체크
        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 수정할 수 없습니다.");
        }

        // 유저가 이 가게 소속인지 확인
        validateOwner(menu.getStore(), user, "본인 가게의 메뉴만 삭제할 수 있습니다.");

        menu.softDelete(user.getId());
    }

    // Helper - 유저의 소속 가게 검증
    private void validateOwner(StoreEntity store, UserEntity user, String errorMessage) {
        if (user.getRole() == Role.OWNER) {
            boolean isMyStore = store.getUsers().stream()
                    .anyMatch(storeUser -> storeUser.getUser().getId().equals(user.getId()));

            if (!isMyStore) {
                throw new AccessDeniedException(errorMessage);
            }
        }
    }
}
