package com.example.Spot.order.infrastructure.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.example.Spot.order.application.service.OrderService;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.infrastructure.event.subscribe.PaymentRefundedEvent;
import com.example.Spot.order.infrastructure.event.subscribe.PaymentSucceededEvent;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.temporal.client.WorkflowClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final WorkflowClient workflowClient;
    
    @KafkaListener(topics = "${spring.kafka.topic.payment.succeeded}", groupId = "${spring.kafka.consumer.group.order}")
    public void handlePaymentSucceeded(String message, Acknowledgment ack) {
        try {
            PaymentSucceededEvent event = objectMapper.readValue(message, PaymentSucceededEvent.class);
            
            OrderResponseDto response = orderService.completePayment(event.getOrderId());
            if (response.getOrderStatus() == OrderStatus.PENDING) {
                log.info("[주문서비스] 결제 성공 이벤트 수신 -> 시그널 전송 시작: OrderID {}", event.getOrderId());
            } else {
                log.info("[멱등성 패스] 이미 최종 상태({})이므로 추가 처리를 생략합니다. OrderID {}",
                        response.getOrderStatus(), event.getOrderId());
            }
            
            ack.acknowledge(); // 성공 시 커밋
            log.info("[Ack 커밋] 메시지 처리 완료: OrderID {}", event.getOrderId());
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
