package com.example.Spot.order.infrastructure.producer;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.Spot.order.event.publish.OrderAcceptedEvent;
import com.example.Spot.order.event.publish.OrderCreatedEvent;
import com.example.Spot.order.event.publish.OrderPendingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${kafka.topic.order.created}")
    private String orderCreatedTopic;
    @Value("${kafka.topic.order.pending}")
    private String orderPendingTopic;
    @Value("${kafka.topic.order.accepted}")
    private String orderAcceptedTopic;
    
    public void sendOrderCreated(UUID orderId, Integer userId, Long amount) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .build();
        send(orderCreatedTopic, orderId.toString(), event);
    }

    public void sendOrderPending(UUID storeId, UUID orderId) {
        OrderPendingEvent event = OrderPendingEvent.builder()
                .storeId(storeId)
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build();
        send(orderPendingTopic, storeId.toString(), event);
    }
    
    public void sendOrderAccepted(Integer userId, UUID orderId, Integer estimatedTime) {
        OrderAcceptedEvent event = OrderAcceptedEvent.builder()
                .userId(userId)
                .orderId(orderId)
                .estimatedTime(estimatedTime)
                .build();
        send(orderAcceptedTopic, userId.toString(), event);
    }
    
    public void send(String topic, String key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, payload);
        } catch (JsonProcessingException e) {
            log.error("❌[Kafka 발행 실패] topic={}, key={}, error={}", topic, key, e.getMessage());
            throw new RuntimeException("이벤트 직렬화 중 오류 발생", e);
        }
    }
}


