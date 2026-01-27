package com.example.Spot.order.infrastructure.producer;

import com.example.Spot.order.domain.entity.OrderOutboxEntity;
import com.example.Spot.order.domain.repository.OrderOutboxRepository;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.Spot.order.infrastructure.event.publish.OrderAcceptedEvent;
import com.example.Spot.order.infrastructure.event.publish.OrderCancelledEvent;
import com.example.Spot.order.infrastructure.event.publish.OrderCreatedEvent;
import com.example.Spot.order.infrastructure.event.publish.OrderPendingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.kafka.topic.order.created}")
    private String orderCreatedTopic;
    @Value("${spring.kafka.topic.order.pending}")
    private String orderPendingTopic;
    @Value("${spring.kafka.topic.order.accepted}")
    private String orderAcceptedTopic;
    @Value("${spring.kafka.topic.order.cancelled}")
    private String orderCancelledTopic;
    
    public void reserveOrderCreated(UUID orderId, Integer userId, Long amount) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .build();
        saveOutbox(orderCreatedTopic, orderId, userId.toString(), event);
    }

    public void reserveOrderPending(UUID storeId, UUID orderId) {
        OrderPendingEvent event = OrderPendingEvent.builder()
                .storeId(storeId)
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build();
        saveOutbox(orderPendingTopic, orderId, storeId.toString(), event);
    }
    
    public void reserveOrderAccepted(Integer userId, UUID orderId, Integer estimatedTime) {
        OrderAcceptedEvent event = OrderAcceptedEvent.builder()
                .userId(userId)
                .orderId(orderId)
                .estimatedTime(estimatedTime)
                .build();
        saveOutbox(orderAcceptedTopic, orderId, userId.toString(), event);
    }
    
    public void reserveOrderCancelled(UUID orderId, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(orderId)
                .reason(reason)
                .build();
        saveOutbox(orderCancelledTopic, orderId, orderId.toString(), event);
    }
    
    public void saveOutbox(String topic, UUID aggregateId, String eventKey, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            
            OrderOutboxEntity outbox = OrderOutboxEntity.builder()
                    .aggregateType("ORDER")
                    .aggregateId(aggregateId)
                    .eventKey(eventKey)
                    .eventType(topic)
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("✅[Outbox 저장 성공] topic:{}, Key:{}, AggregateId:{}", topic, eventKey, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("❌[Outbox 저장 실패] AggregateId={}, error={}", aggregateId, e.getMessage());
            throw new RuntimeException("이벤트 발해 예약 중 오류 발생", e);
        }
    }
    
    public void publish(OrderOutboxEntity outbox) throws Exception {
        kafkaTemplate.send(outbox.getEventType(), outbox.getEventKey(), outbox.getPayload()).get();
    }
}


