package com.example.Spot.order.infrastructure.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.example.Spot.order.application.service.OrderService;
import com.example.Spot.order.infrastructure.event.subscribe.PaymentRefundedEvent;
import com.example.Spot.order.infrastructure.event.subscribe.PaymentSucceededEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "${spring.kafka.topic.payment.succeeded}", groupId = "${spring.kafka.consumer.group.order}")
    public void handlePaymentSucceeded(String message, Acknowledgment ack) {
        try {
            PaymentSucceededEvent event = objectMapper.readValue(message, PaymentSucceededEvent.class);
            orderService.completePayment(event.getOrderId());

            ack.acknowledge(); // 성공 시 커밋
            log.info("[결제 성공] 처리 완료 및 Ack 커밋: OrderID {}", event.getOrderId());
        } catch (Exception e) {
            log.error("[OrderEvent] 결제 성공 이벤트 처리 중 에러 발생: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "${spring.kafka.topic.payment.refunded}", groupId = "${spring.kafka.consumer.group.order}")
    public void handlePaymentRefunded(String message, Acknowledgment ack) {
        try {
            PaymentRefundedEvent event = objectMapper.readValue(message, PaymentRefundedEvent.class);
            log.info(" [결제 환불 완료] 이벤트를 수신했습니다. OrderID: {}", event.getOrderId());
            
            orderService.completeOrderCancellation(event.getOrderId());

            ack.acknowledge(); // 성공 시 커밋
            log.info("[결제 환불] 처리 완료 및 Ack 커밋: OrderID {}", event.getOrderId());
        } catch (Exception e) {
            log.error("[환불 완료 처리 실패] 메시지 소비 중 오류 발생: {}", e.getMessage());
        }
    }
}
