package com.example.Spot.order.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxCleanupScheduler {

    private final OrderOutboxCleanupService cleanupService;

    @Scheduled(cron = "0 0 3 * * *")
    public void run() {
        try {
            cleanupService.cleanup();
        } catch (Exception e) {
            log.error("[ORDER_OUTBOX-CLEANUP] scheduler failed", e);
        }
    }
}
