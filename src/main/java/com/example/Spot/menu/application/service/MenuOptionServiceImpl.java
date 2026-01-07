package com.example.Spot.menu.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuOptionResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuOptionServiceImpl implements MenuOptionService {
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    // 메뉴 옵션 조회
    @Transactional(readOnly = true)
    public List<MenuOptionResponseDto> getOptions(Role userRole, UUID storeId, UUID menuId) {
        // 가게 검증을 위해 메뉴를 먼저 조회
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // URL의 storeId와 실제 메뉴의 storeId가 같은지 검증
        validateMenuBelongsToStore(menu.getStore(), storeId);

        List<MenuOptionEntity> options;

        if (userRole == Role.MANAGER || userRole == Role.MASTER) {
            options = menuOptionRepository.findAllByMenuId(menuId);
        } else {
            options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId);
        }

        return options.stream().map(MenuOptionResponseDto::new).toList();
    }

    // 메뉴 옵션 생성
    @Transactional
    public CreateMenuOptionResponseDto createMenuOption(UserEntity user, UUID storeId, UUID menuId, CreateMenuOptionRequestDto request) {
        MenuEntity menu = menuRepository.findActiveMenuById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // URL의 storeId와 실제 메뉴의 storeId가 같은지 검증
        validateMenuBelongsToStore(menu.getStore(), storeId);

        validatePermission(menu.getStore(), user);

        MenuOptionEntity option = request.toEntity(menu);

        menuOptionRepository.save(option);

        return new CreateMenuOptionResponseDto(option);
    }

    // 메뉴 옵션 업데이트
    @Transactional
    public UpdateMenuOptionResponseDto updateMenuOption(UserEntity user, UUID storeId, UUID optionId, UpdateMenuOptionRequestDto request) {
        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션이 존재하지 않습니다."));

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 옵션은 수정할 수 없습니다.");
        }

        StoreEntity store = option.getMenu().getStore();

        // URL 검증
        validateMenuBelongsToStore(store, storeId);

        // 권한 검증
        validatePermission(store, user);

        // 업데이트 (Dirty Checking)
        option.updateOption(
                request.getName(),
                request.getPrice(),
                request.getDetail()
        );

        if (request.getIsAvailable() != null) {
            option.changeAvailable(request.getIsAvailable());
        }

        return new UpdateMenuOptionResponseDto(option);
    }

    // 메뉴 옵션 삭제
    @Transactional
    public void deleteMenuOption (UserEntity user, UUID storeId, UUID optionId) {
        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션이 존재하지 않습니다."));

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 메뉴입니다.");
        }

        StoreEntity store = option.getMenu().getStore();

        // URL 검증
        validateMenuBelongsToStore(store, storeId);

        // 권한 검증
        validatePermission(store, user);

        option.softDelete(user.getId());
    }

    // Helper
    // 메뉴가 요청된 가게에 소속되어 있는지 확인
    private void validateMenuBelongsToStore(StoreEntity menuStore, UUID requestStoreId) {
        if (!menuStore.getId().equals(requestStoreId)) {
            throw new IllegalArgumentException("잘못된 접근입니다. 해당 메뉴는 요청된 가게에 존재하지 않습니다.");
        }
    }

    private void validatePermission(StoreEntity store, UserEntity user) {
        // 1. 관리자(MASTER)는 무조건 통과
        if (user.getRole() == Role.MASTER || user.getRole() == Role.MANAGER) {
            return;
        }

        // 2. 사장님(OWNER)인 경우
        if (user.getRole() == Role.OWNER) {
            boolean isMyStore = store.getUsers().stream()
                    .anyMatch(storeUser -> storeUser.getUser().getId().equals(user.getId()));

            if (!isMyStore) {
                throw new AccessDeniedException("해당 가게에 대한 수정 권한이 없습니다.");
            }
        } else {
            // 3. 그 외의 Role이 접근 시 차단
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
    }
}
