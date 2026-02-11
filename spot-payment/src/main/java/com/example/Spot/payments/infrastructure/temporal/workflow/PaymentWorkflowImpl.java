package com.example.Spot.payments.infrastructure.temporal.workflow;

import java.time.Duration;
import java.util.UUID;

import com.example.Spot.payments.infrastructure.temporal.activity.PaymentActivities;
import com.example.Spot.payments.infrastructure.temporal.config.PaymentConstants;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

@WorkflowImpl(taskQueues = PaymentConstants.PAYMENT_TASK_QUEUE)
@Slf4j
public class PaymentWorkflowImpl implements PaymentWorkflow {
    
    private final PaymentActivities activities = Workflow.newActivityStub(
            PaymentActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(1))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(10))
                            .setBackoffCoefficient(2.0)
                            .setMaximumInterval(Duration.ofMinutes(1))
                            .setMaximumAttempts(6)
                            .setDoNotRetry(
                                    "com.example.Spot.global.presentation.advice.BillingKeyNotFoundException",
                                    "com.example.Spot.global.presentation.advice.ResourceNotFoundException",
                                    "java.lang.IllegalArgumentException"
                            )
                            .build())
            .build());
    
    @Override
    public void processPayment(UUID paymentId) {
        Saga saga = new Saga(new Saga.Options.Builder().setContinueWithError(false).build());
        
        try {
            saga.addCompensation(activities::recordStatus, paymentId, "ABORTED");
            activities.recordStatus(paymentId, "IN_PROGRESS");
            saga.addCompensation(activities::refundByPaymentId, paymentId);

            activities.executePayment(paymentId);
            activities.publishSucceeded(paymentId);
            
        } catch (Exception e) {
            log.error("[결제워크플로우] 결제 프로세스 중 에러 발생. ID: {}, 사유: {}", paymentId, e.getMessage());
            try {
                saga.compensate();
            } catch (Exception ce) {
                log.error("[결제사가패턴] 보상 트랜잭션 실행 중 치명적 실패!", ce);            
            }
            activities.publishAuthRequired(paymentId, e.getMessage());
            throw e;
        }
    }
}
