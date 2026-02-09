package com.example.Spot.order.infrastructure.temporal.activity;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.order.infrastructure.producer.OrderEventProducer;
import com.example.Spot.order.infrastructure.temporal.config.TemporalConstants;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ActivityImpl(taskQueues = TemporalConstants.ORDER_TASK_QUEUE)
public class OrderActivityImpl implements OrderActivity {
    
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Override
    public void createOrderRecord(OrderCreateRequestDto requestDto, Integer userId, UUID orderId) {
        
    }

    @Override
    @Transactional
    public void completePaymentStatus(UUID orderId) {
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));
        order.completePayment();
    }

    @Override
    @Transactional
    public void handlePaymentFailure(UUID orderId) {
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));
        order.failPayment();
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, String reason) {
        OrderEntity order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음"));
        order.initiateCancel(reason, null);
        orderEventProducer.reserveOrderCancelled(order.getId(), reason);
    }
}
