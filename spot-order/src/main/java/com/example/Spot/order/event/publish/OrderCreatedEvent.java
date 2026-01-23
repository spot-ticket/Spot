package com.example.Spot.order.event.publish;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {
    private UUID orderId;
    private Integer userId;
    private Long amount;
    private String title;
    private String content;
    private String paymentMethod;
}
