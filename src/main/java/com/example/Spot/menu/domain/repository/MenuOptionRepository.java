package com.example.Spot.menu.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

public interface MenuOptionRepository extends JpaRepository<MenuOptionEntity, UUID> {

    // [가게, 손님용] 여러 메뉴의 옵션들 조회 (삭제된 옵션 제외하고 모두 조회)
    List<MenuOptionEntity> findAllByMenuIdInAndIsDeletedFalse(List<UUID> menuId);

    // [관리자용] 여러 메뉴의 옵션들 조회
    List<MenuOptionEntity> findAllByMenuIdIn(List<UUID> menuIds);

    // [관리자용] 특정 메뉴의 모든 옵션 조회 (삭제된 것 포함)
    List<MenuOptionEntity> findAllByMenuId(UUID menuId);

    // [가게, 손님용] 특정 메뉴의 활성 옵션만 조회
    List<MenuOptionEntity> findAllByMenuIdAndIsDeletedFalse(UUID menuId);
}
