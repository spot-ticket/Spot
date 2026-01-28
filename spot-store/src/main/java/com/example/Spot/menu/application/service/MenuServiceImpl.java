package com.example.Spot.menu.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.common.Role;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final MenuOptionRepository menuOptionRepository;

    // ******* //
    // 메뉴 조회 //
    // ******* //
    @Transactional(readOnly = true)
    @Override
    public List<? extends MenuResponseDto> getMenus(UUID storeId, Integer userId, Role userRole) {

        if (userRole == Role.OWNER || userRole == Role.MANAGER || userRole == Role.MASTER) {
            return getMenusForAdmin(storeId, userId, userRole);
        }

        return getMenusForCustomer(storeId);
    }


    @Transactional(readOnly = true)
    public List<MenuAdminResponseDto> getMenusForAdmin(UUID storeId, Integer userId, Role userRole) {
        // 숨김 처리된 메뉴도 같이 조회가 가능함.

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        validateOwner(store, userId, userRole, "본인 가게의 메뉴만 조회할 수 있습니다.");

        boolean isAdmin = userRole == Role.MANAGER || userRole == Role.MASTER;

        List<MenuEntity> menus;

        if (isAdmin) {
            menus = menuRepository.findAllByStoreId(storeId);
        } else {
            menus = menuRepository.findAllByStoreIdAndIsDeletedFalse(storeId);
        }

        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> menuIds = menus.stream().map(MenuEntity::getId).toList();

        List<MenuOptionEntity> allOptions;
        if (isAdmin) {
            allOptions = menuOptionRepository.findAllByMenuIdIn(menuIds);
        } else {
            allOptions = menuOptionRepository.findAllByMenuIdInAndIsDeletedFalse(menuIds);
        }

        // 그룹화 (메모리 작업)
        Map<UUID, List<MenuOptionEntity>> optionsMap = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getMenu().getId()));

        return menus.stream()
                .map(menu -> {
                    List<MenuOptionEntity> options = optionsMap.getOrDefault(menu.getId(), Collections.emptyList());
                    return MenuAdminResponseDto.of(menu, options, userRole);
                })
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<MenuPublicResponseDto> getMenusForCustomer(UUID storeId) {
        // 숨김 처리된 메뉴는 표시가 안됨.

        List<MenuEntity> menus = menuRepository.findAllActiveMenus(storeId);

        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> menuIds = menus.stream().map(MenuEntity::getId).toList();

        List<MenuOptionEntity> allOptions = menuOptionRepository.findAllByMenuIdInAndIsDeletedFalse(menuIds);

        // 4. 그룹화
        Map<UUID, List<MenuOptionEntity>> optionsMap = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getMenu().getId()));

        return menus.stream()
                .map(menu -> {
                        List<MenuOptionEntity> options = optionsMap.getOrDefault(menu.getId(), Collections.emptyList());
                    return MenuPublicResponseDto.of(menu, options);
                }).collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public MenuResponseDto getMenuDetail(UUID storeId, UUID menuId, Integer userId, Role userRole) {

        if (userRole == Role.OWNER || userRole == Role.MANAGER || userRole == Role.MASTER) {
            return getMenuDetailForAdmin(storeId, menuId, userId, userRole);
        }

        return getMenuDetailForCustomer(menuId);
    }

    private MenuAdminResponseDto getMenuDetailForAdmin(UUID storeId, UUID menuId, Integer userId, Role userRole) {
        // 숨김 처리된 것도 보여짐.

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        validateOwner(store, userId, userRole, "본인 가게의 메뉴만 조회할 수 있습니다.");

        MenuEntity menu;
        List<MenuOptionEntity> options;

        boolean isAdmin = userRole == Role.MANAGER || userRole == Role.MASTER;

        if (isAdmin) {
            menu = menuRepository.findByStoreIdAndId(storeId, menuId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

            options = menuOptionRepository.findAllByMenuId(menuId);
        } else {
            menu = menuRepository.findByStoreIdAndIdAndIsDeletedFalse(storeId, menuId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않거나 삭제되었습니다."));

            options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId);
        }

        return MenuAdminResponseDto.of(menu, options, userRole);
    }

    private MenuPublicResponseDto getMenuDetailForCustomer(UUID menuId) {
        // 손님은 활성 메뉴만 조회 가능

        MenuEntity menu = menuRepository.findActiveMenuById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // 옵션 별도 조회 (삭제 안 된 것만)
        List<MenuOptionEntity> options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId);

        return MenuPublicResponseDto.of(menu, options);
    }


    // ******* //
    // 메뉴 생성 //
    // ******* //
    @Transactional
    public CreateMenuResponseDto createMenu(UUID storeId, CreateMenuRequestDto request, Integer userId, Role userRole) {

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        validateOwner(store, userId, userRole, "본인 가게에서만 메뉴를 생성할 수 있습니다.");

        MenuEntity menu = request.toEntity(store);
        menuRepository.save(menu);

        return new CreateMenuResponseDto(menu);
    }

    // ******* //
    // 메뉴 변경 //
    // ******* //
    @Transactional
    public MenuAdminResponseDto updateMenu(UUID storeId, UUID menuId, UpdateMenuRequestDto request, Integer userId, Role userRole) {

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        validateOwner(store, userId, userRole, "본인 가게의 메뉴만 수정할 수 있습니다.");

        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 수정할 수 없습니다.");
        }

        menu.updateMenu(
                request.name(),
                request.price(),
                request.category(),
                request.description(),
                request.imageUrl()
        );

        if (request.isAvailable() != null) {
            menu.changeAvailable(request.isAvailable());
        }

        List<MenuOptionEntity> options;
        boolean isAdmin = userRole == Role.MASTER || userRole == Role.MANAGER;

        if (isAdmin) {
            options = menuOptionRepository.findAllByMenuId(menu.getId());
        } else {
            options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menu.getId());
        }

        // 5) DTO 내부로 로직 이동됨
        return MenuAdminResponseDto.of(menu, options, userRole);
    }

    // ******* //
    // 메뉴 삭제 //
    // ******* //
    @Transactional
    public void deleteMenu(UUID menuId, Integer userId, Role userRole) {

        MenuEntity menu = menuRepository.findByIdWithLock(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 수정할 수 없습니다.");
        }

        validateOwner(menu.getStore(), userId, userRole, "본인 가게의 메뉴만 삭제할 수 있습니다.");

        menu.softDelete(userId);
    }

    // ******* //
    // 메뉴 숨김 //
    // ******* //
    @Transactional
    public void hiddenMenu(UUID menuId, UpdateMenuHiddenRequestDto request, Integer userId, Role userRole) {

        MenuEntity menu = menuRepository.findByIdWithLock(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 숨길 수 없습니다.");
        }

        validateOwner(menu.getStore(), userId, userRole, "본인 가게의 메뉴만 숨길 수 있습니다.");

        menu.changeHidden(request.isHidden());
    }

    private void validateOwner(StoreEntity store, Integer userId, Role userRole, String errorMessage) {
        if (userRole == Role.OWNER) {

            boolean isMyStore = store.getUsers().stream()
                    .anyMatch(storeUser -> storeUser.getUserId().equals(userId));

            if (!isMyStore) {
                throw new AccessDeniedException(errorMessage);
            }
        }
    }
}
