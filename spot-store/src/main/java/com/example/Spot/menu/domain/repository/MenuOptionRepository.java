package com.example.Spot.menu.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;

import jakarta.persistence.LockModeType;

public interface MenuOptionRepository extends JpaRepository<MenuOptionEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mo FROM MenuOptionEntity mo WHERE mo.id = :optionId")
    Optional<MenuOptionEntity> findByIdWithLock(@Param("optionId") UUID optionId);

    List<MenuOptionEntity> findAllByMenuIdInAndIsDeletedFalse(List<UUID> menuId);

    List<MenuOptionEntity> findAllByMenuIdIn(List<UUID> menuIds);

    List<MenuOptionEntity> findAllByMenuId(UUID menuId);

    List<MenuOptionEntity> findAllByMenuIdAndIsDeletedFalse(UUID menuId);
}
