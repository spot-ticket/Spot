package com.example.Spot.order.infrastructure.temporal.workflow;

import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderContextDto;
import java.util.UUID;

import com.example.Spot.order.domain.enums.OrderStatus;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface OrderWorkflow {
    
    @WorkflowMethod
    void processOrder(UUID orderId, Integer userId, OrderCreateRequestDto requestDto, OrderContextDto contextDto);
    
    @SignalMethod
    void signalStatusChanged(OrderStatus nextStatus);

    @SignalMethod
    void signalRefundCompleted();
}
