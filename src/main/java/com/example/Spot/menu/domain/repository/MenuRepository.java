package com.example.Spot.menu.domain.repository;

import com.example.Spot.menu.domain.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID>{

    // [손님용] 가게 메뉴 조회 (삭제 X, 숨김 X)
    List<MenuEntity> findAllByStoreIdAndIsDeletedFalseAndIsHiddenFalse(UUID storeId);

    // [손님용] 메뉴 ID로 조회한 경우 (삭제 X)
    Optional<MenuEntity> findByIdAndIsDeletedFalse(UUID menuId);

    // [가게 주인용] 가게 메뉴 조회 (삭제 X, 숨김 O)
    List<MenuEntity> findAllByStoreIdAndIsDeletedFalse(UUID storeId);
}
