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
    private final OrderOutboxService orderOutboxService;

    @Scheduled(fixedDelay = 1000)
    public void processOutbox() {
        // INIT 상태인 데이터를 오래된 순으로 10개만 가져옴
        List<OrderOutboxEntity> outboxes = outboxRepository.findTop10ReadyTopublish(
                LocalDateTime.now(), PageRequest.of(0, 10));

        if (outboxes.isEmpty()) {
            return;
        }

        for (OrderOutboxEntity outbox : outboxes) {
            orderOutboxService.publishIndividualEvent(outbox);
        }
    }
}
