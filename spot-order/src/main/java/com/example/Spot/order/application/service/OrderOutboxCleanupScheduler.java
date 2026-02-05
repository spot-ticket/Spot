package com.example.Spot.order.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
            log.error("[OUTBOX-CLEANUP] scheduler failed", e);
        }
    }
}
