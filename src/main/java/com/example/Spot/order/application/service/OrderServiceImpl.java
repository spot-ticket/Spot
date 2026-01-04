package com.example.Spot.order.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.entity.OrderItemEntity;
import com.example.Spot.order.domain.entity.OrderItemOptionEntity;
import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemOptionRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    
    private final AtomicInteger orderSequence = new AtomicInteger(0);

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Long userId) {
        StoreEntity store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));

        String orderNumber = generateOrderNumber();

        OrderEntity order = OrderEntity.builder()
                .store(store)
                .userId(userId)
                .orderNumber(orderNumber)
                .pickupTime(requestDto.getPickupTime())
                .needDisposables(requestDto.getNeedDisposables())
                .request(requestDto.getRequest())
                .build();

        for (OrderItemRequestDto itemDto : requestDto.getOrderItems()) {
            MenuEntity menu = menuRepository.findById(itemDto.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다: " + itemDto.getMenuId()));

            if (!menu.getIsAvailable()) {
                throw new IllegalArgumentException("판매 중지된 메뉴입니다: " + menu.getName());
            }

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .menu(menu)
                    .menuPrice(menu.getPrice())
                    .quantity(itemDto.getQuantity())
                    .build();

            for (OrderItemOptionRequestDto optionDto : itemDto.getOptions()) {
                MenuOptionEntity menuOption = menuOptionRepository.findById(optionDto.getMenuOptionId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다: " + optionDto.getMenuOptionId()));

                if (!menuOption.getIsAvailable()) {
                    throw new IllegalArgumentException("선택할 수 없는 옵션입니다: " + menuOption.getName());
                }

                OrderItemOptionEntity orderItemOption = OrderItemOptionEntity.builder()
                        .menuOption(menuOption)
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
    public List<OrderResponseDto> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getUserOrdersByStatus(Long userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatus(userId, status).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getUserActiveOrders(Long userId) {
        return orderRepository.findActiveOrdersByUserId(userId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getStoreOrders(UUID storeId) {
        return orderRepository.findByStoreId(storeId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getStoreOrdersByStatus(UUID storeId, OrderStatus status) {
        return orderRepository.findByStoreIdAndStatus(storeId, status).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getStoreActiveOrders(UUID storeId) {
        return orderRepository.findActiveOrdersByStoreId(storeId).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto acceptOrder(UUID orderId, Integer estimatedTime) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        
        order.acceptOrder(estimatedTime);
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto rejectOrder(UUID orderId, String reason) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        
        order.rejectOrder(reason);
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto startCooking(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        
        order.startCooking();
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto readyForPickup(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        
        order.readyForPickup();
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto completeOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        
        order.completeOrder();
        return OrderResponseDto.from(order);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(UUID orderId, String reason, CancelledBy cancelledBy) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        
        order.cancelOrder(reason, cancelledBy);
        return OrderResponseDto.from(order);
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

    // 주문 번호 생성 (예: ORDER-20260104-0001)
    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = orderSequence.incrementAndGet();
        return String.format("ORDER-%s-%04d", date, sequence);
    }
}

