package com.example.Spot.order.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.order.domain.entity.OrderOutboxEntity;
import com.example.Spot.order.domain.repository.OrderOutboxRepository;
import com.example.Spot.order.infrastructure.producer.OrderEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutboxService {
    
    private final OrderEventProducer orderEventProducer;
    private final OrderOutboxRepository outboxRepository;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishIndividualEvent(OrderOutboxEntity outbox) {
        try {
            orderEventProducer.publish(outbox);
            outbox.markPublished();
            outboxRepository.saveAndFlush(outbox);
            log.info("✅ [Order 발행 성공] ID: {}", outbox.getAggregateId());
        } catch (Exception e) {
            outbox.retryFailed();
            if (outbox.getRetryCount() >= 10) {
                outbox.markFailed();
            }
            outboxRepository.saveAndFlush(outbox);
            log.error("❌ [Order 발행 실패] ID: {}, 사유: {}", outbox.getAggregateId(), e.getMessage());
        }
    }
}
