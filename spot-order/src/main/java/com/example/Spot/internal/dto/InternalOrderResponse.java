package com.example.Spot.internal.dto;

import java.util.UUID;

import com.example.Spot.order.domain.entity.OrderEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalOrderResponse {

    private UUID id;
    private UUID storeId;
    private Integer userId;
    private String orderNumber;
    private String orderStatus;

    public static InternalOrderResponse from(OrderEntity order) {
        return InternalOrderResponse.builder()
                .id(order.getId())
                .storeId(order.getStoreId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus().name())
                .build();
    }
}
