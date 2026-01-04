package com.example.Spot.order.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("SELECT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menu " +
            "LEFT JOIN FETCH oi.orderItemOptions oio " +
            "LEFT JOIN FETCH oio.menuOption " +
            "WHERE o.id = :orderId")
    Optional<OrderEntity> findByIdWithDetails(@Param("orderId") UUID orderId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.userId = :userId " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.userId = :userId " +
            "AND o.orderStatus = :status " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "AND o.orderStatus = :status " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByStoreIdAndStatus(@Param("storeId") UUID storeId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByStoreIdAndDateRange(
            @Param("storeId") UUID storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.userId = :userId " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "AND o.userId = :userId " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByStoreIdAndUserId(
            @Param("storeId") UUID storeId,
            @Param("userId") Integer userId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "AND o.orderStatus IN ('PENDING', 'ACCEPTED', 'COOKING', 'READY') " +
            "ORDER BY o.createdAt ASC")
    List<OrderEntity> findActiveOrdersByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.userId = :userId " +
            "AND o.orderStatus IN ('PAYMENT_PENDING', 'PENDING', 'ACCEPTED', 'COOKING', 'READY') " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findActiveOrdersByUserId(@Param("userId") Integer userId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "AND DATE(o.createdAt) = CURRENT_DATE " +
            "AND o.orderStatus IN ('ACCEPTED', 'COOKING', 'READY') " +
            "ORDER BY o.acceptedAt ASC")
    List<OrderEntity> findTodayActiveOrdersByStoreId(@Param("storeId") UUID storeId);
}

