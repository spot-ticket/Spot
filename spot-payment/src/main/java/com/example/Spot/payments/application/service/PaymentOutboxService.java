package com.example.Spot.payments.application.service;

import com.example.Spot.payments.domain.entity.PaymentOutboxEntity;
import com.example.Spot.payments.domain.repository.PaymentOutboxRepository;
import com.example.Spot.payments.infrastructure.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxService {
    
    private final PaymentEventProducer paymentEventProducer;
    private final PaymentOutboxRepository outboxRepository;
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void publishIndividualEvent(PaymentOutboxEntity outbox) {
        try {
            paymentEventProducer.publish(outbox);
            outbox.markPublished();
            outboxRepository.saveAndFlush(outbox);

            log.info("✅ [결제 이벤트 발행 성공] AggregateId: {}, Topic: {}",
            outbox.getAggregateId(), outbox.getEventType());
        } catch (Exception e) {
            outbox.retryFailed();
            if (outbox.getRetryCount() >= 10) {
                outbox.markFailed();
            }
            outboxRepository.saveAndFlush(outbox);
            log.error("❌ [결제 이벤트 발행 실패] AggregateId: {}, 사유: {}",
                    outbox.getAggregateId(), e.getMessage());
        }
    }
}
