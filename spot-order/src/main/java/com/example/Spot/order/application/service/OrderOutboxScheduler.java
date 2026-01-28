package com.example.Spot.order.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.order.domain.entity.OrderOutboxEntity;
import com.example.Spot.order.domain.repository.OrderOutboxRepository;
import com.example.Spot.order.infrastructure.producer.OrderEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxScheduler {
    
    private final OrderOutboxRepository outboxRepository;
    private final OrderEventProducer orderEventProducer;
    
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutbox() {
        // INIT 상태인 데이터를 오래된 순으로 10개만 가져옴
        List<OrderOutboxEntity> outboxes = outboxRepository.findTop10ReadyTopublish(
                LocalDateTime.now(), PageRequest.of(0, 10));
        
        if (outboxes.isEmpty()) {
            return;
        }
        
        for (OrderOutboxEntity outbox : outboxes) {
            try {
                // 카프카 전송 (토픽명은 eventType)
                // .get()을 사용해 성공/실패 응답을 기다림 (동기 전송)
                orderEventProducer.publish(outbox);
                outbox.markPublished();
                outboxRepository.saveAndFlush(outbox);
                log.info("이벤트 발행 성공: [orderID: {}, eventType: {}", outbox.getAggregateId(), outbox.getEventType());
                
            } catch (Exception e) {
                // 전송 실패 시 지수 백오프 로직 실행
                outbox.retryFailed();
                log.warn("이벤트 발행 실패 - 재시도 횟수: {}, 다음 시도: {}, 사유: {}",
                        outbox.getRetryCount(), outbox.getNextAttemptAt(), e.getMessage());
                // 10회 이상 실패 시 관리 대상으로 전환
                if (outbox.getRetryCount() >= 10) {
                    outbox.markFailed();
                    log.error("최대 재시도 횟수 초과. 수동 조치 필요: [ID: {}]", outbox.getAggregateId());
                }
            }
        }
    }
}
