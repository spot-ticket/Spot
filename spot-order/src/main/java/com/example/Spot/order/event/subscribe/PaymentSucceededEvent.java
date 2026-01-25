package com.example.Spot.order.event.subscribe;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentSucceededEvent {
    private UUID orderId;
    private Integer userId;
    private Long amount;
}
