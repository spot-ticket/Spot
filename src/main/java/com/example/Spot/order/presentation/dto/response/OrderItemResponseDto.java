package com.example.Spot.order.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.order.domain.entity.OrderItemEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemResponseDto {

    private UUID id;
    private UUID menuId;
    private String menuName;
    private BigDecimal menuPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;

    public static OrderItemResponseDto from(OrderItemEntity entity) {
        BigDecimal subtotal = entity.getMenuPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
        
        return OrderItemResponseDto.builder()
                .id(entity.getId())
                .menuId(entity.getMenu().getId())
                .menuName(entity.getMenuName())
                .menuPrice(entity.getMenuPrice())
                .quantity(entity.getQuantity())
                .subtotal(subtotal)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

