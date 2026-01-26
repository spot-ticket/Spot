package com.example.Spot.order.infrastructure.event.subscribe;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRefundedEvent {
    private UUID orderId;
}
