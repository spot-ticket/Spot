package com.example.spotorder.order.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.order.domain.entity.OrderItemOptionEntity;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
                .menuOptionId(entity.getMenuOptionId())
                .optionName(entity.getOptionName())
                .optionDetail(entity.getOptionDetail())
                .optionPrice(entity.getOptionPrice())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

