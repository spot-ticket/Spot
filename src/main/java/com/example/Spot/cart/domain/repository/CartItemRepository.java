package com.example.Spot.cart.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.cart.domain.entity.CartItemEntity;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {

    @Query("SELECT ci FROM CartItemEntity ci " +
            "WHERE ci.cart.id = :cartId AND ci.isDeleted = false")
    List<CartItemEntity> findByCartId(@Param("cartId") UUID cartId);

    @Query("SELECT ci FROM CartItemEntity ci " +
            "LEFT JOIN FETCH ci.options cio " +
            "LEFT JOIN FETCH cio.menuOption " +
            "WHERE ci.cart.id = :cartId AND ci.isDeleted = false")
    List<CartItemEntity> findByCartIdWithOptions(@Param("cartId") UUID cartId);

    @Query("SELECT ci FROM CartItemEntity ci " +
            "WHERE ci.cart.id = :cartId AND ci.menu.id = :menuId AND ci.isDeleted = false")
    Optional<CartItemEntity> findByCartIdAndMenuId(
            @Param("cartId") UUID cartId,
            @Param("menuId") UUID menuId);

    @Query("SELECT ci FROM CartItemEntity ci " +
            "WHERE ci.menu.id = :menuId AND ci.isDeleted = false " +
            "ORDER BY ci.createdAt DESC")
    List<CartItemEntity> findByMenuId(@Param("menuId") UUID menuId);

    @Query("SELECT COUNT(ci) FROM CartItemEntity ci " +
            "WHERE ci.cart.id = :cartId AND ci.isDeleted = false")
    Long countByCartId(@Param("cartId") UUID cartId);
}

