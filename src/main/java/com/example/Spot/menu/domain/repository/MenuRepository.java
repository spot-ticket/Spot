package com.example.Spot.menu.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Spot.menu.domain.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {
}
