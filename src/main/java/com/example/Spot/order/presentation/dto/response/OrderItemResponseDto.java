package com.example.Spot.order.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private List<OrderItemOptionResponseDto> options;
    private BigDecimal optionsTotal;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;

    public static OrderItemResponseDto from(OrderItemEntity entity) {
        List<OrderItemOptionResponseDto> optionDtos = entity.getOrderItemOptions().stream()
                .map(OrderItemOptionResponseDto::from)
                .collect(Collectors.toList());
        
        BigDecimal optionsTotal = optionDtos.stream()
                .map(OrderItemOptionResponseDto::getOptionPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal itemTotal = entity.getMenuPrice().add(optionsTotal);
        BigDecimal subtotal = itemTotal.multiply(BigDecimal.valueOf(entity.getQuantity()));
        
        return OrderItemResponseDto.builder()
                .id(entity.getId())
                .menuId(entity.getMenu().getId())
                .menuName(entity.getMenuName())
                .menuPrice(entity.getMenuPrice())
                .quantity(entity.getQuantity())
                .options(optionDtos)
                .optionsTotal(optionsTotal)
                .subtotal(subtotal)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

