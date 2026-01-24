package com.example.Spot.order.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.feign.PaymentClient;
import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.dto.MenuOptionResponse;
import com.example.Spot.global.feign.dto.MenuResponse;
import com.example.Spot.global.feign.dto.PaymentCancelRequest;
import com.example.Spot.global.feign.dto.PaymentResponse;
import com.example.Spot.global.feign.dto.StoreResponse;
import com.example.Spot.global.feign.dto.StoreUserResponse;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.entity.OrderItemEntity;
import com.example.Spot.order.domain.entity.OrderItemOptionEntity;
import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.order.infrastructure.aop.OrderStatusChange;
import com.example.Spot.order.infrastructure.aop.OrderValidationContext;
import com.example.Spot.order.infrastructure.aop.StoreOwnershipRequired;
import com.example.Spot.order.infrastructure.aop.ValidateStoreAndMenu;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemOptionRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.order.presentation.dto.response.OrderStatsResponseDto;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final StoreClient storeClient;

    @Override
    @Transactional
    @ValidateStoreAndMenu
    public OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Integer userId) {

        // AOP에서 검증한 데이터를 ThreadLocal Context에서 가져옴 (Feign 재호출 없음)
        StoreResponse store = OrderValidationContext.getStoreResponse();

        String orderNumber = generateOrderNumber();

        OrderEntity order = OrderEntity.builder()
                .storeId(store.getId())
                .userId(userId)
                .orderNumber(orderNumber)
                .pickupTime(requestDto.getPickupTime())
                .needDisposables(requestDto.getNeedDisposables())
                .request(requestDto.getRequest())
                .build();

        // Menu도 Context에서 가져옴 (Feign 재호출 없음)
        for (OrderItemRequestDto itemDto : requestDto.getOrderItems()) {
            MenuResponse menu = OrderValidationContext.getMenuResponse(itemDto.getMenuId());

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .menuId(menu.getId())
                    .menuName(menu.getName())
                    .menuPrice(BigDecimal.valueOf(menu.getPrice()))
                    .quantity(itemDto.getQuantity())
                    .build();

            for (OrderItemOptionRequestDto optionDto : itemDto.getOptions()) {
                MenuOptionResponse menuOption = OrderValidationContext.getMenuOptionResponse(optionDto.getMenuOptionId());

                OrderItemOptionEntity orderItemOption = OrderItemOptionEntity.builder()
                        .menuOptionId(menuOption.getId())
                        .optionName(menuOption.getName())
                        .optionDetail(menuOption.getDetail())
                        .optionPrice(BigDecimal.valueOf(menuOption.getPrice()))
                        .build();

                orderItem.addOrderItemOption(orderItemOption);
            }

            order.addOrderItem(orderItem);
        }

        OrderEntity savedOrder = orderRepository.save(order);

        return OrderResponseDto.from(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderById(UUID orderId) {
        OrderEntity order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        return OrderResponseDto.from(order);
    }

    @Override
    public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        return OrderResponseDto.from(order);
    }

    @Override
    public List<OrderResponseDto> getUserOrders(Integer userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getUserActiveOrders(Integer userId) {
        return orderRepository.findActiveOrdersByUserId(userId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getUserOrdersByFilters(Integer userId, UUID storeId, LocalDateTime date, OrderStatus status) {
        List<OrderEntity> orders = getBaseUserOrders(userId, storeId, date);
        return applyFiltersAndMap(orders, date, status);
    }

    private List<OrderResponseDto> getStoreOrdersByFilters(UUID storeId, Integer customerId, LocalDateTime date, OrderStatus status) {
        List<OrderEntity> orders = getBaseStoreOrders(storeId, customerId, date);
        return applyFiltersAndMap(orders, date, status);
    }

    @Override
    @StoreOwnershipRequired
    public List<OrderResponseDto> getChefTodayOrders(Integer userId) {
        UUID storeId = OrderValidationContext.getCurrentStoreId();
        LocalDateTime[] range = getDateRange(LocalDateTime.now());
        return orderRepository.findTodayActiveOrdersByStoreId(storeId, range[0], range[1]).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @StoreOwnershipRequired
    public List<OrderResponseDto> getMyStoreOrders(Integer userId, Integer customerId, LocalDateTime date, OrderStatus status) {
        UUID storeId = OrderValidationContext.getCurrentStoreId();
        return getStoreOrdersByFilters(storeId, customerId, date, status);
    }

    @Override
    @StoreOwnershipRequired
    public List<OrderResponseDto> getMyStoreActiveOrders(Integer userId) {
        UUID storeId = OrderValidationContext.getCurrentStoreId();
        return orderRepository.findActiveOrdersByStoreId(storeId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getAllOrders(UUID storeId, LocalDateTime date, OrderStatus status) {
        List<OrderEntity> orders = getBaseAllOrders(storeId, date);
        return applyFiltersAndMap(orders, date, status);
    }

    // ========== 페이지네이션 ==========

    @Override
    public Page<OrderResponseDto> getUserOrdersWithPagination(
            Integer userId,
            UUID storeId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable) {

        LocalDateTime[] range = date != null ? getDateRange(date) : new LocalDateTime[]{null, null};

        Page<OrderEntity> orderPage = orderRepository.findUserOrdersWithFilters(
                userId,
                storeId,
                status,
                range[0],
                range[1],
                pageable);

        return orderPage.map(OrderResponseDto::from);
    }

    @Override
    @StoreOwnershipRequired
    public Page<OrderResponseDto> getMyStoreOrdersWithPagination(
            Integer userId,
            Integer customerId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable) {

        UUID storeId = OrderValidationContext.getCurrentStoreId();
        LocalDateTime[] range = date != null ? getDateRange(date) : new LocalDateTime[]{null, null};

        Page<OrderEntity> orderPage = orderRepository.findStoreOrdersWithFilters(
                storeId,
                customerId,
                status,
                range[0],
                range[1],
                pageable);

        return orderPage.map(OrderResponseDto::from);
    }

    @Override
    public Page<OrderResponseDto> getAllOrdersWithPagination(
            UUID storeId,
            LocalDateTime date,
            OrderStatus status,
            Pageable pageable) {

        LocalDateTime[] range = date != null ? getDateRange(date) : new LocalDateTime[]{null, null};

        Page<OrderEntity> orderPage = orderRepository.findAllOrdersWithFilters(
                storeId,
                status,
                range[0],
                range[1],
                pageable);

        return orderPage.map(OrderResponseDto::from);
    }

    @Override
    @Transactional
    @OrderStatusChange("ACCEPT")
    public OrderResponseDto acceptOrder(UUID orderId, Integer estimatedTime) {
        OrderEntity order = OrderValidationContext.getCurrentOrder();

        order.acceptOrder(estimatedTime);
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    @OrderStatusChange("REJECT")
    public OrderResponseDto rejectOrder(UUID orderId, String reason) {
        OrderEntity order = OrderValidationContext.getCurrentOrder();

        order.rejectOrder(reason);
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    @OrderStatusChange("COOKING")
    public OrderResponseDto startCooking(UUID orderId) {
        OrderEntity order = OrderValidationContext.getCurrentOrder();

        order.startCooking();
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    @OrderStatusChange("READY")
    public OrderResponseDto readyForPickup(UUID orderId) {
        OrderEntity order = OrderValidationContext.getCurrentOrder();

        order.readyForPickup();
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    @OrderStatusChange("COMPLETE")
    public OrderResponseDto completeOrder(UUID orderId) {
        OrderEntity order = OrderValidationContext.getCurrentOrder();

        order.completeOrder();
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto customerCancelOrder(UUID orderId, String reason) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 주문 취소 처리
        order.cancelOrder(reason, CancelledBy.CUSTOMER);

        // 결제 취소 처리 (Payment 서비스 호출)
        cancelPaymentIfExists(orderId, "고객 주문 취소: " + reason);

        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto storeCancelOrder(UUID orderId, String reason) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 주문 취소 처리
        order.cancelOrder(reason, CancelledBy.STORE);

        // 결제 취소 처리 (Payment 서비스 호출)
        cancelPaymentIfExists(orderId, "가게 주문 취소: " + reason);

        return OrderResponseDto.from(order);
    }

    @CircuitBreaker(name = "payment_ready_create")
    @Bulkhead(name = "payment_ready_create", type = Bulkhead.Type.SEMAPHORE)
    @Retry(name = "payment_ready_create")
    private void cancelPaymentIfExists(UUID orderId, String cancelReason) {
        try {
            // Payment 서비스에서 결제 정보 조회
            if (!paymentClient.existsActivePaymentByOrderId(orderId)) {
                log.info("주문 ID {}에 대한 결제 정보가 없습니다. 결제 취소를 건너뜁니다.", orderId);
                return;
            }

            PaymentResponse payment = paymentClient.getPaymentByOrderId(orderId);

            log.info("결제 취소 요청 - 결제 ID: {}, 주문 ID: {}, 사유: {}", payment.getId(), orderId, cancelReason);

            // Payment 서비스에 결제 취소 요청
            PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                    .cancelReason(cancelReason)
                    .build();
            paymentClient.cancelPayment(payment.getId(), cancelRequest);

            log.info("결제 취소 완료 - 결제 ID: {}", payment.getId());

        } catch (Exception e) {
            log.error("결제 취소 실패 - 주문 ID: {}, 오류: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("결제 취소 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OrderResponseDto completePayment(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        order.completePayment();
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto failPayment(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        order.failPayment();
        return OrderResponseDto.from(order);
    }

    // ========== Private Helper Methods ==========

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePattern = "ORDER-" + date + "-%";

        Optional<String> lastOrderNumber = orderRepository.findTopOrderNumberByDatePattern(datePattern);

        int sequence = 1;
        if (lastOrderNumber.isPresent()) {
            String lastNumber = lastOrderNumber.get();
            String lastSeq = lastNumber.substring(lastNumber.lastIndexOf('-') + 1);
            sequence = Integer.parseInt(lastSeq) + 1;
        }

        return String.format("ORDER-%s-%04d", date, sequence);
    }

    // userId로 storeId 조회 (OWNER, CHEF) - Store 서비스 호출
    private UUID getStoreIdByUserId(Integer userId) {
        StoreUserResponse storeUser = storeClient.getStoreUserByUserId(userId);
        if (storeUser == null) {
            throw new IllegalArgumentException("소속된 매장이 없습니다.");
        }
        return storeUser.getStoreId();
    }

    private List<OrderEntity> getBaseUserOrders(Integer userId, UUID storeId, LocalDateTime date) {
        if (storeId != null && date != null) {
            LocalDateTime[] range = getDateRange(date);
            return orderRepository.findByStoreIdAndUserId(storeId, userId).stream()
                    .filter(o -> isInDateRange(o, range[0], range[1]))
                    .collect(Collectors.toList());
        } else if (storeId != null) {
            return orderRepository.findByStoreIdAndUserId(storeId, userId);
        } else if (date != null) {
            LocalDateTime[] range = getDateRange(date);
            return orderRepository.findByUserIdAndDateRange(userId, range[0], range[1]);
        } else {
            return orderRepository.findByUserId(userId);
        }
    }

    private List<OrderEntity> getBaseStoreOrders(UUID storeId, Integer customerId, LocalDateTime date) {
        if (customerId != null && date != null) {
            LocalDateTime[] range = getDateRange(date);
            return orderRepository.findByStoreIdAndUserId(storeId, customerId).stream()
                    .filter(o -> isInDateRange(o, range[0], range[1]))
                    .collect(Collectors.toList());
        } else if (customerId != null) {
            return orderRepository.findByStoreIdAndUserId(storeId, customerId);
        } else if (date != null) {
            LocalDateTime[] range = getDateRange(date);
            return orderRepository.findByStoreIdAndDateRange(storeId, range[0], range[1]);
        } else {
            return orderRepository.findByStoreId(storeId);
        }
    }

    private List<OrderEntity> getBaseAllOrders(UUID storeId, LocalDateTime date) {
        if (storeId != null && date != null) {
            LocalDateTime[] range = getDateRange(date);
            return orderRepository.findAllOrdersByStoreIdAndDateRange(storeId, range[0], range[1]);
        } else if (storeId != null) {
            return orderRepository.findAllOrdersByStoreId(storeId);
        } else if (date != null) {
            LocalDateTime[] range = getDateRange(date);
            return orderRepository.findAllOrdersByDateRange(range[0], range[1]);
        } else {
            return orderRepository.findAllOrders();
        }
    }

    private List<OrderResponseDto> applyFiltersAndMap(List<OrderEntity> orders, LocalDateTime date, OrderStatus status) {
        return orders.stream()
                .filter(o -> status == null || o.getOrderStatus().equals(status))
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    private LocalDateTime[] getDateRange(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);
        return new LocalDateTime[] { startOfDay, endOfDay };
    }

    private boolean isInDateRange(OrderEntity order, LocalDateTime start, LocalDateTime end) {
        return order.getCreatedAt().isAfter(start) && order.getCreatedAt().isBefore(end);
    }

    @Override
    public OrderStatsResponseDto getOrderStats() {
        List<OrderEntity> allOrders = orderRepository.findAll();

        long totalOrders = allOrders.size();

        BigDecimal totalRevenue = allOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(item -> item.getMenuPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> statusCounts = allOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderStatus().name(),
                        Collectors.counting()
                ));

        List<OrderStatsResponseDto.OrderStatusStats> orderStatusStats = statusCounts.entrySet().stream()
                .map(entry -> OrderStatsResponseDto.OrderStatusStats.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return OrderStatsResponseDto.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .orderStatusStats(orderStatusStats)
                .build();
    }
}
