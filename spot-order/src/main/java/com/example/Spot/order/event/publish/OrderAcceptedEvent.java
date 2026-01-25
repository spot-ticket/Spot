package com.example.Spot.order.event.publish;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAcceptedEvent {
    private Integer userId;
    private UUID orderId;
    private Integer estimatedTime;
}
