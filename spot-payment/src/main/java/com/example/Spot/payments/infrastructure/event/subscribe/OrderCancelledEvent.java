package com.example.Spot.payments.infrastructure.event.subscribe;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OrderCancelledEvent {
    private UUID orderId;
    private String reason;
}
