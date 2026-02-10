package com.example.Spot.payments.infrastructure.temporal.workflow;

import java.util.UUID;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PaymentWorkflow {
    @WorkflowMethod
    void processPayment(UUID paymentId);
}
