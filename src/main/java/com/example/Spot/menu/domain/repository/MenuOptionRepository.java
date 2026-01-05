package com.example.Spot.menu.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

public interface MenuOptionRepository extends JpaRepository<MenuOptionEntity, UUID> {

    // [가게, 손님용] 메뉴 옵션 조회 (삭제된 옵션 제외하고 모두 조회)
    List<MenuOptionEntity> findAllByMenuIdAndIsDeletedFalse(UUID menuId);

    // [관리자용] 메뉴 옵션 조회
    List<MenuOptionEntity> findAllByMenuId(UUID menuId);
}
