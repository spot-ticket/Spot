package com.example.Spot.order.infrastructure.event.publish;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancelledEvent {
    private UUID orderId;
    private String reason;
}
