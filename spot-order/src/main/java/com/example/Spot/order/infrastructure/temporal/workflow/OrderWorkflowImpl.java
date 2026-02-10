package com.example.Spot.order.infrastructure.temporal.workflow;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.Spot.order.infrastructure.temporal.activity.OrderActivity;
import com.example.Spot.order.infrastructure.temporal.config.OrderConstants;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

@Component
@WorkflowImpl(taskQueues = OrderConstants.ORDER_TASK_QUEUE)
public class OrderWorkflowImpl implements OrderWorkflow {

    private static final ActivityOptions ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(5).build())
            .build();
    
    private boolean isPaid = false;
    
    @Override
    public void processOrder(UUID orderId) {
        OrderActivity activities = Workflow.newActivityStub(OrderActivity.class, ACTIVITY_OPTIONS);
        boolean received = Workflow.await(Duration.ofMinutes(15), () -> isPaid);
        
        if (received) {
            activities.completePaymentStatus(orderId);
        } else {
            activities.handlePaymentFailure(orderId);
            activities.cancelOrder(orderId, "결제 시간 초과로 인한 자동 취소");
        }
    }

    @Override
    public void signalPaymentCompleted() {
        this.isPaid = true;
    }
}
