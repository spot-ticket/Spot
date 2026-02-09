package com.example.Spot.order.infrastructure.temporal.activity;

import java.util.UUID;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderActivity {
    
    @ActivityMethod
    void completePaymentStatus(UUID orderId);

    @ActivityMethod
    void handlePaymentFailure(UUID orderId);

    @ActivityMethod
    void cancelOrder(UUID orderId, String reason);

}
