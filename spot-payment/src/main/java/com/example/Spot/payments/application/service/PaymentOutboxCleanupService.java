package com.example.Spot.payments.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.payments.domain.repository.PaymentOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxCleanupService {
    
    private static final int RETENTION_DAYS = 7;
    private final PaymentOutboxRepository paymentOutboxRepository;
    
    @Transactional
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deletedCount = paymentOutboxRepository.deleteOlderThan(threshold);
        
        if (deletedCount > 0) {
            log.info("[PAYMENT_OUTBOX-CLEANUP] deleted {} rows (threshold={})", deletedCount, threshold);
        }
    }
}
