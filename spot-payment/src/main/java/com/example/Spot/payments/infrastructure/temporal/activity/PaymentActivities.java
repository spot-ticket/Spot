package com.example.Spot.payments.infrastructure.temporal.activity;

import java.util.UUID;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface PaymentActivities {
    
    void recordStatus(UUID paymentId, String status);
    
    String executePayment(UUID paymentId);
    
    void refundByPaymentId(UUID paymentId);
    
    void publishSucceeded(UUID paymentId);
    
    void publishAuthRequired(UUID paymentId, String message);
}
