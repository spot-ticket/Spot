package com.example.Spot.global.feign.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private UUID storeId;
    private String storeName;
    private Integer userId;
    private String orderStatus;
    private BigDecimal totalPrice;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private UUID menuId;
        private String menuName;
        private BigDecimal menuPrice;
        private Integer quantity;
    }
}
