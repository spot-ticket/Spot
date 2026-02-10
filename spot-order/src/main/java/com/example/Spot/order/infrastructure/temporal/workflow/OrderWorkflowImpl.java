package com.example.Spot.order.infrastructure.temporal.workflow;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.Spot.order.domain.enums.OrderStatus;
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
    
    private OrderStatus currentStatus = OrderStatus.PAYMENT_PENDING;
    
    @Override
    public void processOrder(UUID orderId) {
        OrderActivity activities = Workflow.newActivityStub(OrderActivity.class, ACTIVITY_OPTIONS);
        
        boolean paidWithinTime = Workflow.await(Duration.ofMinutes(15),
                () -> currentStatus == OrderStatus.PENDING || currentStatus.isFinalStatus());
        Workflow.getLogger(OrderWorkflowImpl.class).info("15분 타이머 완료");


        if (!paidWithinTime && currentStatus == OrderStatus.PAYMENT_PENDING) {
            OrderStatus actualStatus = activities.getOrderStatus(orderId);
            if (actualStatus == OrderStatus.PENDING) {
                this.currentStatus = OrderStatus.PENDING;
            }
        }
        
        if (!currentStatus.isPaid()) {
            activities.handlePaymentFailure(orderId);
            activities.cancelOrder(orderId, "결제 시간 초과로 인한 자동 취소");
            return;
        }
        if (shouldStop(OrderStatus.PENDING)) {
            return;
        }
        
        Workflow.await(Duration.ofMinutes(30),
                () -> currentStatus == OrderStatus.ACCEPTED || currentStatus.isFinalStatus());
        Workflow.getLogger(OrderWorkflowImpl.class).info("30분 타이머 완료");

        if (shouldStop(OrderStatus.ACCEPTED)) {
            return;
        }
        
        Workflow.await(() -> currentStatus == OrderStatus.COOKING || currentStatus.isFinalStatus());
        if (shouldStop(OrderStatus.COOKING)) {
            return;
        }

        Workflow.await(() -> currentStatus == OrderStatus.READY || currentStatus.isFinalStatus());
        if (shouldStop(OrderStatus.READY)) {
            return;
        }

        Workflow.await(() -> currentStatus == OrderStatus.COMPLETED || currentStatus.isFinalStatus());
    }

    @Override
    public void signalStatusChanged(OrderStatus nextStatus) {
        this.currentStatus = nextStatus;
    }

    private boolean shouldStop(OrderStatus targetStatus) {
        return currentStatus != targetStatus && currentStatus.isFinalStatus();
    }
}
