package com.example.Spot.order.infrastructure.event.subscribe;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentSucceededEvent {
    private UUID orderId;
    private Integer userId;
}
