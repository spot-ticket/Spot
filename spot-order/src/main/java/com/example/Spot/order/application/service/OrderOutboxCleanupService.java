package com.example.Spot.order.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.order.domain.repository.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutboxCleanupService {

    private static final int RETENTION_DAYS = 7;
    private final OrderOutboxRepository orderOutboxRepository;

    @Transactional
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deletedCount = orderOutboxRepository.deleteOlderThan(threshold);

        if (deletedCount > 0) {
            log.info("[ORDER_OUTBOX-CLEANUP] deleted {} rows (threshold={})", deletedCount, threshold);
        }
    }
}
