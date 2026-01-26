package com.example.Spot.order.infrastructure.listener;

import org.springframework.kafka.annotation.KafkaListener;
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
    
    @KafkaListener(topics = "${kafka.topic.payment.succeeded}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentSucceeded(String message) {
        try {
            // 1. 이벤트 역직렬화
            PaymentSucceededEvent event = objectMapper.readValue(message, PaymentSucceededEvent.class);
            
            // 2. 서비스 로직 호출
            orderService.completePayment(event.getOrderId());
            
        } catch (Exception e) {
            log.error("❌ [OrderService] 결제 성공 이벤트 처리 중 에러 발생: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "${kafka.topic.payment.refunded}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentRefunded(String message) {
        try {
            PaymentRefundedEvent event = objectMapper.readValue(message, PaymentRefundedEvent.class);
            log.info(" [결제 환불 완료] 이벤트를 수신했습니다. OrderID: {}", event.getOrderId());
            
            orderService.completeOrderCancellation(event.getOrderId());
            
        } catch (Exception e) {
            log.error("❌ [환불 완료 처리 실패] 메시지 소비 중 오류 발생: {}", e.getMessage());
        }
    }
}
