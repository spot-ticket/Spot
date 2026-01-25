package com.example.Spot.order.infrastructure.producer;

import java.util.UUID;

import com.example.Spot.order.event.publish.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${kafka.topic.order.created}")
    private String orderCreatedTopic;
    
    public void sendOrderCreated(UUID orderId, Integer userId, Long amount) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .build();

        try {
            String payload = objectMapper.writeValueAsString(event);
            log.info("주문 생성 이벤트 발행 시작: topic={}, orderId={}, amount={}", orderCreatedTopic, orderId, amount);
            kafkaTemplate.send(orderCreatedTopic, payload);

        } catch (
                JsonProcessingException e) {
            log.error("OrderCreatedEvent 직렬화 실패 - orderId: {}", orderId, e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
        
    }
}


