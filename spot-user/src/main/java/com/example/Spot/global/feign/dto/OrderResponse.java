package com.example.Spot.global.feign.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private UUID orderId;
    private Integer userId;
    private UUID storeId;
    private String orderNumber;
    private LocalDateTime pickupTime;
    private LocalDateTime createdAt;
    private String status;
    private BigDecimal totalAmount;
    private String storeName;
}
