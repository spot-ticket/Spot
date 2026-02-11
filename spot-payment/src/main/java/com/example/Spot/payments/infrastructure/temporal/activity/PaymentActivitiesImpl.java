package com.example.Spot.payments.infrastructure.temporal.activity;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.payments.application.service.PaymentHistoryService;
import com.example.Spot.payments.application.service.command.PaymentApprovalService;
import com.example.Spot.payments.application.service.command.PaymentCancellationService;
import com.example.Spot.payments.application.service.query.PaymentQueryService;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.infrastructure.event.publish.AuthRequiredEvent;
import com.example.Spot.payments.infrastructure.producer.PaymentEventProducer;
import com.example.Spot.payments.infrastructure.temporal.config.PaymentConstants;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ActivityImpl(taskQueues = PaymentConstants.PAYMENT_TASK_QUEUE)
public class PaymentActivitiesImpl implements PaymentActivities {

    private final PaymentApprovalService paymentApprovalService;
    private final PaymentCancellationService paymentCancellationService;
    private final PaymentQueryService paymentQueryService;
    private final PaymentHistoryService paymentHistoryService;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    @Transactional
    public void recordStatus(UUID paymentId, String status) {
        try {
            if ("IN_PROGRESS".equals(status)) {
                paymentHistoryService.recordPaymentProgress(paymentId);
            } else if ("ABORTED".equals(status)) {
                paymentHistoryService.recordFailure(paymentId, new RuntimeException("Workflow terminated/failed"));
            }
        } catch (Exception e) {
            log.info("[Activity] 상태 기록 중 알림: {}", e.getMessage());
        }
    }

    @Override
    public String executePayment(UUID paymentId) {
        // 기존 ApprovalService 호출 -> 내부에서 Toss 호출 및 Success 이력 기록까지 수행됨
        PaymentResponseDto.Confirm confirm = paymentApprovalService.createPaymentBillingApprove(paymentId);
        return confirm.paymentKey();
    }


    @Override
    @Transactional
    public void refundByPaymentId(UUID paymentId) {
        PaymentEntity payment = paymentQueryService.findPayment(paymentId);
        paymentCancellationService.refundByOrderId(payment.getOrderId());
    }

    @Override
    @Transactional
    public void publishSucceeded(UUID paymentId) {
        PaymentEntity payment = paymentQueryService.findPayment(paymentId);
        paymentEventProducer.reservePaymentSucceededEvent(payment.getOrderId(), payment.getUserId());
    }

    @Override
    @Transactional
    public void publishAuthRequired(UUID paymentId, String message) {
        PaymentEntity payment = paymentQueryService.findPayment(paymentId);
        AuthRequiredEvent event = AuthRequiredEvent.builder()
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .message(message)
                .build();
        paymentEventProducer.reserveAuthRequiredEvent(event);
    }
}
