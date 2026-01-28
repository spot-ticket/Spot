package com.example.Spot.payments.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.Spot.payments.domain.entity.PaymentOutboxEntity;
import com.example.Spot.payments.domain.repository.PaymentOutboxRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentOutboxScheduler {

    private final PaymentOutboxRepository outboxRepository;
    private final PaymentOutboxService paymentOutboxService;

    @Scheduled(fixedDelay = 1000)
    public void processOutbox() {
        List<PaymentOutboxEntity> outboxes = outboxRepository.findTop10ReadyTopublish(
                LocalDateTime.now(), PageRequest.of(0, 10));

        if (outboxes.isEmpty()) {
            return;
        }

        for (PaymentOutboxEntity outbox : outboxes) {
            paymentOutboxService.publishIndividualEvent(outbox);
        }
    }
}
