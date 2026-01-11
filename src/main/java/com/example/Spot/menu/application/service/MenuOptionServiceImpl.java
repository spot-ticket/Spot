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
import com.example.Spot.user.domain.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuOptionServiceImpl implements MenuOptionService {
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    // 메뉴 옵션 생성
    @Transactional
    public CreateMenuOptionResponseDto createMenuOption(UUID storeId, UUID menuId, Integer userId, Role userRole, CreateMenuOptionRequestDto request) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, userId, userRole, "본인 가게의 메뉴만 생성할 수 있습니다.");

        // 해당 메뉴가 가게에 있는지 체크
        MenuEntity menu = menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = request.toEntity(menu);

        menuOptionRepository.save(option);

        return CreateMenuOptionResponseDto.from(option);
    }

    // 메뉴 옵션 업데이트
    @Transactional
    public MenuOptionAdminResponseDto updateMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionRequestDto request) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, userId, userRole, "본인 가게의 메뉴 옵션만 변경할 수 있습니다.");

        // 해당 메뉴가 가게에 있는지 체크
        menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴 옵션이 존재하지 않습니다."));

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 옵션입니다.");
        }

        // URL 경로의 menuId와 실제 DB 데이터가 일치하는지 검증
        if (!option.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("해당 메뉴에 속하지 않는 옵션입니다.");
        }

        // 업데이트 (Dirty Checking)
        option.updateOption(request.name(), request.price(), request.detail());

        // 메뉴 옵션 품절 여부 체크
        if (request.isAvailable() != null) {
            option.changeAvailable(request.isAvailable());
        }

        return MenuOptionAdminResponseDto.of(option, userRole);
    }

    // 메뉴 옵션 삭제
    @Transactional
    public void deleteMenuOption (UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, userId, userRole, "본인 가게의 메뉴 옵션만 삭제할 수 있습니다.");

        // 해당 메뉴가 가게에 있는지 체크
        menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴의 옵션이 존재하지 않습니다."));


        // URL 경로의 menuId와 실제 DB 데이터가 일치하는지 검증
        if (!option.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("해당 메뉴에 속하지 않는 옵션입니다.");
        }

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 옵션입니다.");
        }

        option.softDelete(userId);
    }

    // 메뉴 숨김
    @Transactional
    public void hiddenMenuOption(UUID storeId, UUID menuId, UUID optionId, Integer userId, Role userRole, UpdateMenuOptionHiddenRequestDto request) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, userId, userRole, "본인 가게의 메뉴 옵션만 숨길 수 있습니다.");

        // 메뉴 조회
        menuRepository.findByStoreIdAndId(storeId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴의 옵션이 존재하지 않습니다."));

        if (!option.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("해당 메뉴에 속하지 않는 옵션입니다.");
        }

        // 삭제 여부 체크
        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴 옵션은 숨길 수 없습니다.");
        }

        // 숨김 처리
        option.changeHidden(request.isHidden());
    }

    // Helper - 유저의 소속 가게 검증
    private void validateOwner(StoreEntity store, Integer userId, Role userRole, String errorMessage) {
        if (userRole == Role.OWNER) {

            // StoreEntity가 가지고 있는 User 목록(store.getUsers())을 순회하며
            // 현재 로그인한 userId와 일치하는지 확인
            boolean isMyStore = store.getUsers().stream()
                    .anyMatch(storeUser -> storeUser.getUser().getId().equals(userId));

            if (!isMyStore) {
                throw new AccessDeniedException(errorMessage);
            }
        }
    }
}
