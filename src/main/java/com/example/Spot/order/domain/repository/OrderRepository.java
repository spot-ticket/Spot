package com.example.Spot.order.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menu " +
            "WHERE o.id = :orderId")
    Optional<OrderEntity> findByIdWithDetails(@Param("orderId") UUID orderId);

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.userId = :userId " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByStoreId(@Param("storeId") UUID storeId);

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
            "AND o.createdAt >= :startOfDay AND o.createdAt < :endOfDay " +
            "AND o.orderStatus IN ('ACCEPTED', 'COOKING', 'READY') " +
            "ORDER BY o.acceptedAt ASC")
    List<OrderEntity> findTodayActiveOrdersByStoreId(
            @Param("storeId") UUID storeId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // MASTER/MANAGER 전용 - 전체 매장 주문 조회
    @Query("SELECT o FROM OrderEntity o " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllOrders();

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllOrdersByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllOrdersByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllOrdersByStoreIdAndDateRange(
            @Param("storeId") UUID storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ========== 페이지네이션 쿼리 (JOIN FETCH + 필터링) ==========

    // 고객 주문 조회 (페이지네이션)
    @Query(value = "SELECT DISTINCT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menu m " +
            "WHERE o.userId = :userId " +
            "AND (CAST(:storeId AS string) IS NULL OR o.store.id = :storeId) " +
            "AND (CAST(:status AS string) IS NULL OR o.orderStatus = :status) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)",
            countQuery = "SELECT COUNT(DISTINCT o) FROM OrderEntity o " +
            "WHERE o.userId = :userId " +
            "AND (CAST(:storeId AS string) IS NULL OR o.store.id = :storeId) " +
            "AND (CAST(:status AS string) IS NULL OR o.orderStatus = :status) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<OrderEntity> findUserOrdersWithFilters(
            @Param("userId") Integer userId,
            @Param("storeId") UUID storeId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 점주 매장 주문 조회 (페이지네이션)
    @Query(value = "SELECT DISTINCT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menu m " +
            "WHERE o.store.id = :storeId " +
            "AND (CAST(:customerId AS string) IS NULL OR o.userId = :customerId) " +
            "AND (CAST(:status AS string) IS NULL OR o.orderStatus = :status) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)",
            countQuery = "SELECT COUNT(DISTINCT o) FROM OrderEntity o " +
            "WHERE o.store.id = :storeId " +
            "AND (CAST(:customerId AS string) IS NULL OR o.userId = :customerId) " +
            "AND (CAST(:status AS string) IS NULL OR o.orderStatus = :status) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<OrderEntity> findStoreOrdersWithFilters(
            @Param("storeId") UUID storeId,
            @Param("customerId") Integer customerId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 관리자 전체 주문 조회 (페이지네이션)
    @Query(value = "SELECT DISTINCT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.store s " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menu m " +
            "WHERE (CAST(:storeId AS string) IS NULL OR o.store.id = :storeId) " +
            "AND (CAST(:status AS string) IS NULL OR o.orderStatus = :status) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)",
            countQuery = "SELECT COUNT(DISTINCT o) FROM OrderEntity o " +
            "WHERE (CAST(:storeId AS string) IS NULL OR o.store.id = :storeId) " +
            "AND (CAST(:status AS string) IS NULL OR o.orderStatus = :status) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<OrderEntity> findAllOrdersWithFilters(
            @Param("storeId") UUID storeId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 주문 번호 생성을 위한 날짜별 마지막 주문 번호 조회
    @Query(value = "SELECT order_number FROM p_order " +
            "WHERE order_number LIKE :datePattern " +
            "ORDER BY order_number DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<String> findTopOrderNumberByDatePattern(@Param("datePattern") String datePattern);
}

