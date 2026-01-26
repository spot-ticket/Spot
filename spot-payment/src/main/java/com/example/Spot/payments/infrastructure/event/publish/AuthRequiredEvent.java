package com.example.Spot.payments.infrastructure.event.publish;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequiredEvent {
    private UUID orderId;
    private Integer userId;
    private String message;
}
