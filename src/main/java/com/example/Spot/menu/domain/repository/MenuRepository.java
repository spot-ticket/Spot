package com.example.Spot.menu.domain.repository;

import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {
}
