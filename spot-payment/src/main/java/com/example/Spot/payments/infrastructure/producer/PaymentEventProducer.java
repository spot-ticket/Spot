package com.example.Spot.payments.infrastructure.producer;

import com.example.Spot.payments.domain.entity.PaymentOutboxEntity;
import com.example.Spot.payments.domain.repository.PaymentOutboxRepository;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.Spot.payments.infrastructure.event.publish.AuthRequiredEvent;
import com.example.Spot.payments.infrastructure.event.publish.PaymentRefundedEvent;
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
    private final PaymentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.kafka.topic.payment-auth.required}")
    private String authRequiredTopic;
    @Value("${spring.kafka.topic.payment.succeeded}")
    private String paymentSucceededTopic;
    @Value("${spring.kafka.topic.payment.refunded}")
    private String paymentRefundedTopic;
    
    public void reserveAuthRequiredEvent(AuthRequiredEvent event) {
        saveOutbox(authRequiredTopic, event.getOrderId(), event);
    }
    
    public void reservePaymentSucceededEvent(UUID orderId, Integer userId) {
        PaymentSucceededEvent event = new PaymentSucceededEvent(orderId, userId);
        saveOutbox(paymentSucceededTopic, orderId, event);
    }
    
    public void reservePaymentRefundedEvent(UUID orderId) {
        PaymentRefundedEvent event = new PaymentRefundedEvent(orderId);
        saveOutbox(paymentRefundedTopic, orderId, event);
    }
    
    private void saveOutbox(String topic, UUID aggregateId, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            PaymentOutboxEntity outbox = PaymentOutboxEntity.builder()
                    .aggregateType("PAYMENT")
                    .aggregateId(aggregateId)
                    .eventType(topic)
                    .payload(payload)
                    .build();
            
            outboxRepository.save(outbox);
            log.info("✅[Payment Outbox 저장 성공] topic:{}, AggregateId:{}", topic, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("❌[Payment Outbox 저장 실패] AggregateId={}, error={}", aggregateId, e.getMessage());
            throw new RuntimeException("이벤트 발행 예약 중 오류 발생", e);
        }
    }
    
    public void publish(PaymentOutboxEntity outbox) throws Exception {
        kafkaTemplate.send(outbox.getEventType(), outbox.getAggregateId().toString(), outbox.getPayload()).get();
    }
}
