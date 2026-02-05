package com.example.Spot.payments.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxCleanupScheduler {

    private final PaymentOutboxCleanupService cleanupService;

    @Scheduled(cron = "0 0 3 * * *")
    public void run() {
        try {
            cleanupService.cleanup();
        } catch (Exception e) {
            log.error("[PAYMENT_OUTBOX-CLEANUP] scheduler failed", e);
        }
    }
}
