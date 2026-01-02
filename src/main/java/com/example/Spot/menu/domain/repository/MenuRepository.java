package com.example.Spot.menu.domain.repository;

import com.example.Spot.menu.domain.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {
}
