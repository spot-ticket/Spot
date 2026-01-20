package com.example.Spot.menu.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.menu.domain.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {

    // [손님용] 메뉴 전체 조회 (삭제 X, 숨김 X)
    @Query("select m from MenuEntity m where m.store.id = :storeId AND m.isDeleted = false AND m.isHidden = false")
    List<MenuEntity> findAllActiveMenus(@Param("storeId") UUID storeId);

    // [손님용] 메뉴 상세 조회
    // 1. DISTINCT: 1:N 조인 시 데이터 중복 방지 (JPA 엔티티 중복 제거)
    // 2. 옵션이 없는 메뉴도 조회되어야 하므로 'LEFT' 사용
    // [추가해야 할 기능] LEFT JOIN FETCH m.options: 메뉴를 가져올 때 옵션들도 '한 번에' 가져옴 (N+1 방지)
    @Query("SELECT DISTINCT m FROM MenuEntity m " +
            "WHERE m.id = :menuId " +
            "AND m.isDeleted = false " +
            "AND m.isHidden = false")
    Optional<MenuEntity> findActiveMenuById(@Param("menuId") UUID menuId);

    // [가게용] 메뉴 전체 조회 (삭제 X, 숨김 O)
    List<MenuEntity> findAllByStoreIdAndIsDeletedFalse(UUID storeId);

    // [가게용] 메뉴 상세 조회 (삭제된 건 제외)
    Optional<MenuEntity> findByStoreIdAndIdAndIsDeletedFalse(UUID storeId, UUID menuId);

    // [관리자용] 메뉴 전체 조회
    List<MenuEntity> findAllByStoreId(UUID storeId);

    // [관리자용] 메뉴 상세 조회 (삭제 여부 상관없음)
    Optional<MenuEntity> findByStoreIdAndId(UUID storeId, UUID menuId);
}
