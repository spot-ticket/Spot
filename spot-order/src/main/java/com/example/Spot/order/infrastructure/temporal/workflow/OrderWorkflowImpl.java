package com.example.Spot.order.infrastructure.temporal.workflow;

import com.example.Spot.order.domain.enums.CancelledBy;
import com.example.Spot.order.infrastructure.temporal.dto.OrderStatusUpdate;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderContextDto;
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

    private OrderStatus currentStatus = OrderStatus.PAYMENT_PENDING;
    private Integer estimatedTime;
    private String reason;
    private CancelledBy actor;
    private boolean isRefundCompleted = false;

    private static final ActivityOptions ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(5).build())
            .build();

    @Override
    public void processOrder(UUID orderId, Integer userId, OrderCreateRequestDto requestDto, OrderContextDto contextDto) {
        OrderActivity activities = Workflow.newActivityStub(OrderActivity.class, ACTIVITY_OPTIONS);
        activities.createOrderInDb(orderId, userId, requestDto, contextDto);

        Workflow.await(Duration.ofMinutes(5),
                () -> currentStatus == OrderStatus.PENDING || currentStatus.isFinalStatus() || currentStatus == OrderStatus.CANCEL_PENDING);
        if (currentStatus == OrderStatus.PENDING) {
            activities.updateOrderStatusInDb(orderId, OrderStatus.PENDING, null, null, null);
        } else {
            handleCancelOrRejectIfNecessary(orderId, activities, "결제 단계 취소/타임아웃");
            return;
        }

        boolean isAccepted = Workflow.await(Duration.ofSeconds(15),
                () -> currentStatus == OrderStatus.ACCEPTED || currentStatus == OrderStatus.CANCEL_PENDING || currentStatus == OrderStatus.REJECT_PENDING || currentStatus.isFinalStatus());

        if (isAccepted && currentStatus == OrderStatus.ACCEPTED) {
            activities.updateOrderStatusInDb(orderId, OrderStatus.ACCEPTED, this.estimatedTime, null, null);
        } else {
            if (!isAccepted) {
                this.currentStatus = OrderStatus.CANCEL_PENDING;
                this.reason = "타임아웃으로 인한 자동취소";
                this.actor = CancelledBy.SYSTEM;
            }
            handleCancelOrRejectIfNecessary(orderId, activities, "점주 미수락/거절");
            return;
        }

        // 4. 조리 단계 (COOKING)
        if (waitForStatusAndUpdate(orderId, OrderStatus.COOKING, activities)) return;
        if (waitForStatusAndUpdate(orderId, OrderStatus.READY, activities)) return;
        if (waitForStatusAndUpdate(orderId, OrderStatus.COMPLETED, activities)) return;
    }

    private boolean handleCancelOrRejectIfNecessary(UUID orderId, OrderActivity activities, String defaultReason) {
        if (currentStatus == OrderStatus.CANCEL_PENDING || currentStatus == OrderStatus.REJECT_PENDING) {
            String finalReason = (this.reason != null) ? this.reason : defaultReason;
            CancelledBy finalActor = this.actor;
            if (currentStatus == OrderStatus.CANCEL_PENDING && finalActor == null) {
                finalActor = CancelledBy.SYSTEM;
            }

            activities.updateOrderStatusInDb(orderId, currentStatus, null, finalReason, finalActor);
            waitForRefundAndFinalize(orderId, activities);
            return true;
        }
        return currentStatus.isFinalStatus();
    }

    private void waitForRefundAndFinalize(UUID orderId, OrderActivity activities) {
        boolean isSuccess = Workflow.await(Duration.ofMinutes(30), () -> isRefundCompleted);
        if (isSuccess) {
            activities.finalizeOrder(orderId);
        } else {
            activities.handleRefundTimeout(orderId);
        }
    }

    private boolean waitForStatusAndUpdate(UUID orderId, OrderStatus targetStatus, OrderActivity activities) {
        Workflow.await(() -> currentStatus == targetStatus || currentStatus == OrderStatus.CANCEL_PENDING
                || currentStatus.isFinalStatus());
        if (currentStatus == targetStatus) {
            activities.updateOrderStatusInDb(orderId, targetStatus, null, null, null);
            return false;
        }
        handleCancelOrRejectIfNecessary(orderId, activities, "진행 중 취소/거절");
        return true;
    }

    @Override
    public void signalStatusChanged(OrderStatusUpdate update) {
        if (this.currentStatus.isFinalStatus()) {
            return;
        }
        if (update.getStatus() == OrderStatus.REJECT_PENDING) {
            if (this.currentStatus != OrderStatus.PENDING) {
                return;
            }
        }
        this.currentStatus = update.getStatus();
        this.estimatedTime = update.getEstimatedTime();
        this.reason = update.getReason();
        this.actor = update.getCancelledBy();
    }

    @Override
    public void signalRefundCompleted() {
        this.isRefundCompleted = true;
    }
}
