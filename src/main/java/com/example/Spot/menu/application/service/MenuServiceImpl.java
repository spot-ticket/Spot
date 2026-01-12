package com.example.Spot.menu.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final MenuOptionRepository menuOptionRepository;

    // 통합 메뉴 조회
    @Transactional(readOnly = true)
    @Override
    public List<? extends MenuResponseDto> getMenus(UUID storeId, Integer userId, Role userRole) {

        // 3. 관리자 권한(사장, 매니저, 마스터)이면 Admin용 로직 호출
        if (userRole == Role.OWNER || userRole == Role.MANAGER || userRole == Role.MASTER) {
            // 관리자용
            return getMenusForAdmin(storeId, userId, userRole);
        }

        // 손님용
        return getMenusForCustomer(storeId);
    }

    // [관리자/가게] 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuAdminResponseDto> getMenusForAdmin(UUID storeId, Integer userId, Role userRole) {

        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 본인 가게 확인
        validateOwner(store, userId, userRole, "본인 가게의 메뉴만 조회할 수 있습니다.");

        boolean isAdmin = userRole == Role.MANAGER || userRole == Role.MASTER;

        List<MenuEntity> menus;

        // 관리자(마스터, 매니저, 오너)인지 아닌지 판단
        // 메뉴 리스트 조회
        if (isAdmin) {
            menus = menuRepository.findAllByStoreId(storeId);
        } else {
            menus = menuRepository.findAllByStoreIdAndIsDeletedFalse(storeId);
        }

        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 메뉴 ID 추출
        List<UUID> menuIds = menus.stream().map(MenuEntity::getId).toList();

        // 5. 옵션 일괄 조회 (쿼리 2 - N+1 방지)
        List<MenuOptionEntity> allOptions;
        if (isAdmin) {
            allOptions = menuOptionRepository.findAllByMenuIdIn(menuIds);
        } else {
            // 오너는 여기서 걸러져서 "삭제 안 된 옵션"만 가져옴
            allOptions = menuOptionRepository.findAllByMenuIdInAndIsDeletedFalse(menuIds);
        }

        // 6. 그룹화 (메모리 작업)
        Map<UUID, List<MenuOptionEntity>> optionsMap = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getMenu().getId()));

        // 7. DTO 변환
        return menus.stream()
                .map(menu -> {
                    List<MenuOptionEntity> options = optionsMap.getOrDefault(menu.getId(), Collections.emptyList());
                    return MenuAdminResponseDto.of(menu, options, userRole);
                })
                .collect(Collectors.toList());
    }

    // [손님] 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuPublicResponseDto> getMenusForCustomer(UUID storeId) {

        List<MenuEntity> menus = menuRepository.findAllActiveMenus(storeId);

        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 메뉴 ID 추출
        List<UUID> menuIds = menus.stream().map(MenuEntity::getId).toList();

        // 3. 옵션 일괄 조회 (삭제 안 된 것만)
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

    // 통합 메뉴 상세 조회
    @Transactional(readOnly = true)
    @Override
    public MenuResponseDto getMenuDetail(UUID storeId, UUID menuId, Integer userId, Role userRole) {

        // 관리자 권한(사장, 매니저, 마스터)이면 Admin용 로직 호출
        if (userRole == Role.OWNER || userRole == Role.MANAGER || userRole == Role.MASTER) {
            // 관리자용
            return getMenuDetailForAdmin(storeId, menuId, userId, userRole);
        }

        // 손님용
        return getMenuDetailForCustomer(menuId);
    }

    // [관리자용] 메뉴 상세 조회
    private MenuAdminResponseDto getMenuDetailForAdmin(UUID storeId, UUID menuId, Integer userId, Role userRole) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 본인 가게 확인
        validateOwner(store, userId, userRole, "본인 가게의 메뉴만 조회할 수 있습니다.");

        MenuEntity menu;
        List<MenuOptionEntity> options;

        boolean isAdmin = userRole == Role.MANAGER || userRole == Role.MASTER;

        if (isAdmin) {
            // 관리자: 삭제된 것도 포함 조회
            menu = menuRepository.findByStoreIdAndId(storeId, menuId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

            options = menuOptionRepository.findAllByMenuId(menuId);
        } else {
            // 점주
            menu = menuRepository.findByStoreIdAndIdAndIsDeletedFalse(storeId, menuId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않거나 삭제되었습니다."));

            options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId);
        }

        return MenuAdminResponseDto.of(menu, options, userRole);
    }

    // [손님용] 메뉴 상세 조회
    private MenuPublicResponseDto getMenuDetailForCustomer(UUID menuId) {
        // 손님은 활성 메뉴만 조회 가능
        MenuEntity menu = menuRepository.findActiveMenuById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // 옵션 별도 조회 (삭제 안 된 것만)
        List<MenuOptionEntity> options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId);

        return MenuPublicResponseDto.of(menu, options);
    }

    // 4. 메뉴 생성
    @Transactional
    public CreateMenuResponseDto createMenu(UUID storeId, CreateMenuRequestDto request, Integer userId, Role userRole) {

        // 1) 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, userId, userRole, "본인 가게에서만 메뉴를 생성할 수 있습니다.");

        MenuEntity menu = request.toEntity(store);
        menuRepository.save(menu);

        return new CreateMenuResponseDto(menu);
    }

    // 5. 메뉴 수정
    @Transactional
    public MenuAdminResponseDto updateMenu(UUID storeId, UUID menuId, UpdateMenuRequestDto request, Integer userId, Role userRole) {

        // 1) 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 메뉴 조회
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, userId, userRole, "본인 가게의 메뉴만 수정할 수 있습니다.");

        // 삭제 여부 체크
        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 수정할 수 없습니다.");
        }

        // 메뉴 수정
        menu.updateMenu(
                request.name(),
                request.price(),
                request.category(),
                request.description(),
                request.imageUrl()
        );

        // 메뉴 품절 여부 변경
        if (request.isAvailable() != null) {
            menu.changeAvailable(request.isAvailable());
        }

        List<MenuOptionEntity> options;
        boolean isAdmin = userRole == Role.MASTER || userRole == Role.MANAGER;

        if (isAdmin) {
            // 관리자: 삭제된 옵션 포함 '전부' 조회
            options = menuOptionRepository.findAllByMenuId(menu.getId());
        } else {
            // 점주: 삭제된 옵션 제외하고 '살아있는 것만' 조회
            options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menu.getId());
        }

        // 5) DTO 내부로 로직 이동됨
        return MenuAdminResponseDto.of(menu, options, userRole);
    }

    // 6. 메뉴 삭제
    @Transactional
    public void deleteMenu(UUID menuId, Integer userId, Role userRole) {

        // 메뉴 조회
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        // 삭제 여부 체크
        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 수정할 수 없습니다.");
        }

        // 유저가 이 가게 소속인지 확인
        validateOwner(menu.getStore(), userId, userRole, "본인 가게의 메뉴만 삭제할 수 있습니다.");

        menu.softDelete(userId);
    }

    // 7. 메뉴 숨김
    @Transactional
    public void hiddenMenu(UUID menuId, UpdateMenuHiddenRequestDto request, Integer userId, Role userRole) {

        // 메뉴 조회
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        // 삭제 여부 체크
        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 숨길 수 없습니다.");
        }

        // 유저가 이 가게 소속인지 확인
        validateOwner(menu.getStore(), userId, userRole, "본인 가게의 메뉴만 숨길 수 있습니다.");

        // 숨김 처리
        menu.changeHidden(request.isHidden());
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
