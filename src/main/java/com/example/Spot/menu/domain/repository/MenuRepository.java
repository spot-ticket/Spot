package com.example.Spot.menu.domain.repository;

import com.example.Spot.menu.domain.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID>{

    // [손님용] 가게 메뉴 조회 (삭제 X, 숨김 X)
    @Query("select m from MenuEntity m where m.store.id = :storeId AND m.isDeleted = false AND m.isHidden = false")
    List<MenuEntity> findAllActiveMenus(@Param("storeId") UUID storeId);

    // [손님용] 메뉴 ID로 조회한 경우 (삭제 X, 숨김 X)
    @Query("select m from MenuEntity m where m.id = :menuId AND m.isDeleted = false AND m.isHidden = false")
    Optional<MenuEntity> findActiveMenuById(@Param("menuId") UUID menuId);

    // [가게 주인용] 가게 메뉴 조회 (삭제 X, 숨김 O)
    List<MenuEntity> findAllByStoreIdAndIsDeletedFalse(UUID storeId);
}
