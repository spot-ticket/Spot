package com.example.Spot.order.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.order.domain.entity.OrderItemOptionEntity;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemOptionEntity, UUID> {

    @Query("SELECT oio FROM OrderItemOptionEntity oio " +
            "WHERE oio.orderItem.id = :orderItemId")
    List<OrderItemOptionEntity> findByOrderItemId(@Param("orderItemId") UUID orderItemId);

    @Query("SELECT oio FROM OrderItemOptionEntity oio " +
            "WHERE oio.menuOptionId = :menuOptionId " +
            "ORDER BY oio.createdAt DESC")
    List<OrderItemOptionEntity> findByMenuOptionId(@Param("menuOptionId") UUID menuOptionId);

    // Order Entity를 통해 Order Item과 Order Item Option을 한 번에 조회하는 로직을 분해함.
    @Query("SELECT oio FROM OrderItemOptionEntity oio " +
            "WHERE oio.orderItem.id IN :orderItemIds")
    List<OrderItemOptionEntity> findByOrderItemIdIn(@Param("orderItemIds") List<UUID> orderItemIds);
}

