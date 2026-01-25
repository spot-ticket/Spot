package com.example.Spot.order.event.publish;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPendingEvent {
    private UUID storeId;
    private UUID orderId;
    private LocalDateTime timestamp;
}
