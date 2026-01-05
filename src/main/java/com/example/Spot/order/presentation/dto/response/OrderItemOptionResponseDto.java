package com.example.Spot.order.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.order.domain.entity.OrderItemOptionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItemOptionResponseDto {

    private UUID id;
    private UUID menuOptionId;
    private String optionName;
    private String optionDetail;
    private BigDecimal optionPrice;
    private LocalDateTime createdAt;

    public static OrderItemOptionResponseDto from(OrderItemOptionEntity entity) {
        return OrderItemOptionResponseDto.builder()
                .id(entity.getId())
                .menuOptionId(entity.getMenuOption().getId())
                .optionName(entity.getOptionName())
                .optionDetail(entity.getOptionDetail())
                .optionPrice(entity.getOptionPrice())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

