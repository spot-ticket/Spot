package com.example.Spot.menu.application.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionAdminResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.global.common.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuOptionServiceImpl implements MenuOptionService {
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    @Override
    @Transactional
    public CreateMenuOptionResponseDto createMenuOption(UUID storeId, UUID menuId, Integer userId, Role userRole, CreateMenuOptionRequestDto request) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        validateOwner(store, userId, userRole, "본인 가게의 메뉴 옵션만 생성할 수 있습니다.");

        // 해당 메뉴가 가게에 있는지 체크
        MenuEntity menu = menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = request.toEntity(menu);
        menuOptionRepository.save(option);

        return CreateMenuOptionResponseDto.from(option);
    }

    @Override
    @Transactional
    public MenuOptionAdminResponseDto updateMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionRequestDto request) {

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        validateOwner(store, userId, userRole, "본인 가게의 메뉴 옵션만 수정할 수 있습니다.");

        menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴 옵션이 존재하지 않습니다."));

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 옵션입니다.");
        }

        if (!option.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("해당 메뉴에 속하지 않는 옵션입니다.");
        }

        option.updateOption(request.name(), request.price(), request.detail());

        if (request.isAvailable() != null) {
            option.changeAvailable(request.isAvailable());
        }

        return MenuOptionAdminResponseDto.of(option, userRole);
    }

    @Override
    @Transactional
    public void deleteMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole) {

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        validateOwner(store, userId, userRole, "본인 가게의 메뉴 옵션만 삭제할 수 있습니다.");

        menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴의 옵션이 존재하지 않습니다."));


        if (!option.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("해당 메뉴에 속하지 않는 옵션입니다.");
        }

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 옵션입니다.");
        }

        option.softDelete(userId);
    }

    @Override
    @Transactional
    public void hiddenMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionHiddenRequestDto request) {

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        validateOwner(store, userId, userRole, "본인 가게의 메뉴 옵션만 숨길 수 있습니다.");

        menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴의 옵션이 존재하지 않습니다."));

        if (!option.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("해당 메뉴에 속하지 않는 옵션입니다.");
        }

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴 옵션은 숨길 수 없습니다.");
        }

        option.changeHidden(request.isHidden());
    }

    private void validateOwner(StoreEntity store, Integer userId, Role userRole, String errorMessage) {
        if (userRole == Role.MASTER || userRole == Role.MANAGER) {
            return;
        }
        boolean isOwner = store.getUsers().stream()
                .anyMatch(su -> su.getUserId().equals(userId));
        if (!isOwner) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
