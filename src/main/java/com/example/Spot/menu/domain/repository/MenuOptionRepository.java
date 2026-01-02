package com.example.Spot.menu.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

public interface MenuOptionRepository extends JpaRepository<MenuOptionEntity, UUID> {

    // [손님용] 메뉴 옵션 조회 (구매 가능한 옵션)
    @Query("select mo from MenuOptionEntity mo where mo.menu.id = :menuId AND mo.isDeleted = false  AND mo.isAvailable = true")
    List<MenuOptionEntity> findAllActiveOptions(@Param("menuId") UUID menuId);

    // [가게용] 메뉴 옵션 조회 (삭제된 옵션 제외하고 모두 조회)
    List<MenuOptionEntity> findAllByMenuIdAndIsDeletedFalse(UUID menuId);
}
