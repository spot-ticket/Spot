package com.example.Spot.cart.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.cart.domain.entity.CartEntity;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, UUID> {

    Optional<CartEntity> findByUserIdAndIsDeletedFalse(Integer userId);

    @Query("SELECT c FROM CartEntity c " +
            "LEFT JOIN FETCH c.store s " +
            "LEFT JOIN FETCH c.items ci " +
            "LEFT JOIN FETCH ci.menu m " +
            "LEFT JOIN FETCH ci.options cio " +
            "LEFT JOIN FETCH cio.menuOption " +
            "WHERE c.userId = :userId AND c.isDeleted = false")
    Optional<CartEntity> findByUserIdWithItems(@Param("userId") Integer userId);

    boolean existsByUserIdAndIsDeletedFalse(Integer userId);

    @Query("SELECT c FROM CartEntity c " +
            "WHERE c.store.id = :storeId AND c.isDeleted = false")
    Optional<CartEntity> findByStoreIdAndIsDeletedFalse(@Param("storeId") UUID storeId);
}
