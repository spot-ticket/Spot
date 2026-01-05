package com.example.Spot.menu.application.service;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.menu.presentation.dto.request.CreateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuOptionRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuOptionResponseDto;
import com.example.Spot.menu.presentation.dto.response.UpdateMenuOptionResponseDto;
import com.example.Spot.user.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuOptionServiceImpl implements MenuOptionService{
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    // 메뉴 옵션 조회
    @Transactional(readOnly = true)
    public List<MenuOptionResponseDto> getOptions(UUID menuId, Role userRole) {

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
    public CreateMenuOptionResponseDto createMenuOption(UUID menuId, CreateMenuOptionRequestDto request) {
        MenuEntity menu = menuRepository.findActiveMenuById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        MenuOptionEntity option = request.toEntity(menu);

        menuOptionRepository.save(option);

        return new CreateMenuOptionResponseDto(option);
    }

    // 메뉴 옵션 업데이트
    @Transactional
    public UpdateMenuOptionResponseDto updateMenuOption(UUID optionId, UpdateMenuOptionRequestDto request) {
        // 1. 업데이트할 옵션을 optionId로 찾기
        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션이 존재하지 않습니다."));

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 옵션은 수정할 수 없습니다.");
        }

        // 2. 업데이트 메서드 추출 (Dirty Checking)
        option.updateOption(
                request.getName(),
                request.getPrice(),
                request.getDetail()
        );

        // 3. 품절 여부 변경 (null이 아닐 때만)
        if (request.getIsAvailable() != null) {
            option.changeAvailable(request.getIsAvailable());
        }

        return new UpdateMenuOptionResponseDto(option);
    }

    // 메뉴 옵션 삭제
    @Transactional
    public void deleteMenuOption (UUID optionId) {
        MenuOptionEntity option = menuOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션이 존재하지 않습니다."));

        if (option.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 메뉴입니다.");
        }

        option.softDelete();
    }
}
