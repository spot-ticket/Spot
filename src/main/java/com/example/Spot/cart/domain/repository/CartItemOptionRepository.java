package com.example.Spot.cart.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.cart.domain.entity.CartItemOptionEntity;

@Repository
public interface CartItemOptionRepository extends JpaRepository<CartItemOptionEntity, UUID> {

    @Query("SELECT cio FROM CartItemOptionEntity cio " +
            "WHERE cio.cartItem.id = :cartItemId")
    List<CartItemOptionEntity> findByCartItemId(@Param("cartItemId") UUID cartItemId);

    @Query("SELECT cio FROM CartItemOptionEntity cio " +
            "LEFT JOIN FETCH cio.menuOption mo " +
            "WHERE cio.cartItem.id = :cartItemId")
    List<CartItemOptionEntity> findByCartItemIdWithMenuOption(@Param("cartItemId") UUID cartItemId);

    @Query("SELECT cio FROM CartItemOptionEntity cio " +
            "WHERE cio.menuOption.id = :menuOptionId")
    List<CartItemOptionEntity> findByMenuOptionId(@Param("menuOptionId") UUID menuOptionId);
}

