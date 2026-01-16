package com.example.Spot.order.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.domain.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderResponseDto {

    private UUID id;
    private Integer userId;
    private UUID storeId;
    private String storeName;
    private String orderNumber;
    private Boolean needDisposables;
    private String request;
    private LocalDateTime pickupTime;
    private OrderStatus orderStatus;
    private Integer estimatedTime;
    private String reason;
    private CancelledBy cancelledBy;
    
    private LocalDateTime paymentCompletedAt;
    private LocalDateTime paymentFailedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cookingStartedAt;
    private LocalDateTime cookingCompletedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    
    private List<OrderItemResponseDto> orderItems;
    
    private BigDecimal totalAmount;
        
    public static OrderResponseDto from(OrderEntity entity) {
        List<OrderItemResponseDto> orderItemDtos = entity.getOrderItems().stream()
                .map(OrderItemResponseDto::from)
                .collect(Collectors.toList());
        
        BigDecimal totalAmount = orderItemDtos.stream()
                .map(OrderItemResponseDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return OrderResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .storeId(entity.getStore().getId())
                .storeName(entity.getStore().getName())
                .orderNumber(entity.getOrderNumber())
                .needDisposables(entity.getNeedDisposables())
                .request(entity.getRequest())
                .pickupTime(entity.getPickupTime())
                .orderStatus(entity.getOrderStatus())
                .estimatedTime(entity.getEstimatedTime())
                .reason(entity.getReason())
                .cancelledBy(entity.getCancelledBy())
                .paymentCompletedAt(entity.getPaymentCompletedAt())
                .paymentFailedAt(entity.getPaymentFailedAt())
                .acceptedAt(entity.getAcceptedAt())
                .rejectedAt(entity.getRejectedAt())
                .cookingStartedAt(entity.getCookingStartedAt())
                .cookingCompletedAt(entity.getCookingCompletedAt())
                .pickedUpAt(entity.getPickedUpAt())
                .cancelledAt(entity.getCancelledAt())
                .createdAt(entity.getCreatedAt())
                .orderItems(orderItemDtos)
                .totalAmount(totalAmount)
                .build();
    }
}

