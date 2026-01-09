package com.example.Spot.menu.application.service;

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
import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;
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
    public List<? extends MenuResponseDto> getMenus(UUID storeId, Integer userId) {

        // 로그인 유저 조회 (Role 확인을 위해 DB 조회 필수)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        Role role = user.getRole();

        // 3. 관리자 권한(사장, 매니저, 마스터)이면 Admin용 로직 호출
        if (role == Role.OWNER || role == Role.MANAGER || role == Role.MASTER) {
            // 관리자용
            return getMenusForAdmin(storeId, user);
        }

        // 손님용
        return getMenusForCustomer(storeId);
    }

    // [관리자/가게] 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuAdminResponseDto> getMenusForAdmin(UUID storeId, UserEntity user) {

        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 본인 가게 확인
        validateOwner(store, user, "본인 가게의 메뉴만 조회할 수 있습니다.");

        List<MenuEntity> menus;
        boolean isAdmin = user.getRole() == Role.MANAGER || user.getRole() == Role.MASTER;

        // 관리자(마스터, 매니저, 오너)인지 아닌지 판단
        if (isAdmin) {
            menus = menuRepository.findAllByStoreId(storeId);
        } else {
            menus = menuRepository.findAllByStoreIdAndIsDeletedFalse(storeId);
        }

        // 3) DTO 변환 (옵션 포함)
        return menus.stream()
                .map(menu -> {
                    List<MenuOptionEntity> options = isAdmin
                            ? menuOptionRepository.findAllByMenuId(menu.getId())
                            : menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menu.getId());

                    return new MenuAdminResponseDto(menu, options);
                })
                .collect(Collectors.toList());
    }

    // [손님] 메뉴 조회
    @Transactional(readOnly = true)
    public List<MenuPublicResponseDto> getMenusForCustomer(UUID storeId) {

        List<MenuEntity> menus = menuRepository.findAllActiveMenus(storeId);

        return menus.stream().map(menu -> {
            // 손님은 삭제되지 않은 옵션만 조회
            List<MenuOptionEntity> options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menu.getId());

            return MenuPublicResponseDto.of(menu, options);
        }).collect(Collectors.toList());
    }

    // 통합 메뉴 상세 조회
    @Transactional(readOnly = true)
    @Override
    public MenuResponseDto getMenuDetail(UUID storeId, UUID menuId, Integer userId) {

        // 유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        Role role = user.getRole();

        // 관리자 권한(사장, 매니저, 마스터)이면 Admin용 로직 호출
        if (role == Role.OWNER || role == Role.MANAGER || role == Role.MASTER) {
            // 관리자용
            return getMenuDetailForAdmin(storeId, menuId, user);
        }

        // 손님용
        return getMenuDetailForCustomer(menuId);
    }

    // [관리자용] 메뉴 상세 조회
    private MenuAdminResponseDto getMenuDetailForAdmin(UUID storeId, UUID menuId, UserEntity user) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 본인 가게 확인
        validateOwner(store, user, "본인 가게의 메뉴만 조회할 수 있습니다.");

        MenuEntity menu;
        List<MenuOptionEntity> options;
        boolean isAdmin = user.getRole() == Role.MANAGER || user.getRole() == Role.MASTER;

        if (isAdmin) {
            // 관리자: 삭제된 것도 포함 조회
            menu = menuRepository.findByStoreIdAndId(storeId, menuId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));
            options = menuOptionRepository.findAllByMenuId(menuId);
        } else {
            menu = menuRepository.findByStoreIdAndIdAndIsDeletedFalse(storeId, menuId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않거나 삭제되었습니다."));
            options = menuOptionRepository.findAllByMenuIdAndIsDeletedFalse(menuId);
        }

        return new MenuAdminResponseDto(menu, options);
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
    public CreateMenuResponseDto createMenu(UUID storeId, CreateMenuRequestDto request, Integer userId) {

        // 1) 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 2) 유저 조회 (Optional 처리 수정)
        UserEntity user = userRepository.findById(userId) // findRoleById -> findById 권장
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, user, "본인 가게에서만 메뉴를 생성할 수 있습니다.");

        MenuEntity menu = request.toEntity(store);
        menuRepository.save(menu);

        return new CreateMenuResponseDto(menu);
    }

    // 5. 메뉴 수정
    @Transactional
    public UpdateMenuResponseDto updateMenu(UUID storeId, UUID menuId, UpdateMenuRequestDto request, Integer userId) {

        // 1) 가게 조회
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게가 존재하지 않습니다."));

        // 2) 유저 조회 (Optional 처리 수정)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 메뉴 조회
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        // 삭제 여부 체크
        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 수정할 수 없습니다.");
        }

        // 유저가 이 가게 소속인지 확인
        validateOwner(store, user, "본인 가게의 메뉴만 수정할 수 있습니다.");

        // 메뉴 수정
        menu.updateMenu(
                request.getName(),
                request.getPrice(),
                request.getCategory(),
                request.getDescription(),
                request.getImageUrl()
        );

        // 품절 여부 (값이 왔을 때만)
        if (request.getIsAvailable() != null) {
            menu.changeAvailable(request.getIsAvailable());
        }

        return new UpdateMenuResponseDto(menu);
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

    // 7. 메뉴 숨김
    @Transactional
    public void hiddenMenu(UUID menuId, UpdateMenuHiddenRequestDto request, UserEntity user) {

        // 메뉴 조회
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 없습니다."));

        // 삭제 여부 체크
        if (menu.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 메뉴는 숨길 수 없습니다.");
        }

        // 유저가 이 가게 소속인지 확인
        validateOwner(menu.getStore(), user, "본인 가게의 메뉴만 숨길 수 있습니다.");

        // 숨김 처리
        menu.changeHidden(request.getIsHidden());
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
