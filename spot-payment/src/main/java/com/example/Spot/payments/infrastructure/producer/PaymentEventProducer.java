package com.example.Spot.payments.infrastructure.producer;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.Spot.payments.infrastructure.event.publish.AuthRequiredEvent;
import com.example.Spot.payments.infrastructure.event.publish.PaymentSucceededEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${kafka.topic.payment-auth.required}")
    private String authRequiredTopic;
    @Value("${kafka.topic.payment.succeeded}")
    private String paymentSucceededTopic;
    
    public void sendAuthRequiredEvent(AuthRequiredEvent authRequiredEvent) {
        try {
            String payload = objectMapper.writeValueAsString(authRequiredEvent);
            log.info("인증 필요 이벤트 발행: topic={}, orderId={}", authRequiredTopic, authRequiredEvent.getOrderId());
            kafkaTemplate.send(authRequiredTopic, payload);
        } catch (JsonProcessingException e) {
            log.error("AuthRequireEvent 직렬화 실패 - orderId: {}", authRequiredEvent.getOrderId(), e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }
    
    public void sendPaymentSucceededEvent(UUID orderId, Integer userId) {
        try {
            PaymentSucceededEvent event = new PaymentSucceededEvent(orderId, userId);
            String payload = objectMapper.writeValueAsString(event);
            
            log.info("주문 확정 요청 이벤트 발행: topic={}, orderId={}",
                    paymentSucceededTopic, orderId);
            
            kafkaTemplate.send(paymentSucceededTopic, payload);
        } catch (JsonProcessingException e) {
            log.error("PaymentSucceededEvent 직렬화 실패 - orderId: {}", orderId, e);
        }
    }
}
