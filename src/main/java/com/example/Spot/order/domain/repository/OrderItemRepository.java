package com.example.Spot.order.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.order.domain.entity.OrderItemEntity;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

    @Query("SELECT oi FROM OrderItemEntity oi " +
            "WHERE oi.order.id = :orderId")
    List<OrderItemEntity> findByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT oi FROM OrderItemEntity oi " +
            "WHERE oi.menu.id = :menuId " +
            "ORDER BY oi.createdAt DESC")
    List<OrderItemEntity> findByMenuId(@Param("menuId") UUID menuId);
}

