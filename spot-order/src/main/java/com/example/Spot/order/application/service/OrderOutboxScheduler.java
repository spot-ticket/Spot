package com.example.Spot.order.application.service;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.order.domain.entity.OrderOutboxEntity;
import com.example.Spot.order.domain.enums.OutboxStatus;
import com.example.Spot.order.domain.repository.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxScheduler {
    
    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutbox() {
        // INIT 상태인 데이터를 오래된 순으로 10개만 가져옴
        List<OrderOutboxEntity> outboxes = outboxRepository.findTop10ByOutboxStatusOrderByCreatedAtAsc(OutboxStatus.INIT);
        
        if (outboxes.isEmpty()) {
            return;
        }
        
        for (OrderOutboxEntity outbox : outboxes) {
            try {
                // 카프카 전송 (토픽명은 eventType)
                // .get()을 사용해 성공/실패 응답을 기다림 (동기 전송)
                kafkaTemplate.send(outbox.getEventType(), outbox.getPayload()).get();
            } catch (Exception e) {
                // 전송 실패 시 처리
                log.error("이벤트 발행 실패 - ID: {}, 에러: {}", outbox.getAggregateId(), e.getMessage());
                outbox.incrementRetryCount();
                
                // 5회 이상 실패 시 FAILED 상태로 전환하여 관리자 확인 대상으로 분류
                
            }
        }
        
        
        
    }
}
