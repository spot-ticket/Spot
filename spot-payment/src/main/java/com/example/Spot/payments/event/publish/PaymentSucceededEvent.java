package com.example.Spot.payments.event.publish;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSucceededEvent {
    private UUID orderId;
    private Integer userId;
    private Long amount;
}
