package com.example.Spot.payments.infrastructure.listener;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.Spot.global.presentation.advice.BillingKeyNotFoundException;
import com.example.Spot.payments.event.publish.AuthRequiredEvent;
import com.example.Spot.payments.infrastructure.producer.PaymentEventProducer;
import com.example.Spot.payments.application.service.PaymentService;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.event.subscribe.OrderCreatedEvent;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(topics = "${kafka.topic.order.created}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("주문 생성 이벤트 수신: orderId={}", event.getOrderId());

            // 1. 부족한 정보를 채워 DTO를 조립합니다.
            PaymentRequestDto.Confirm confirmRequest = PaymentRequestDto.Confirm.builder()
                    .title("Spot 주문 결제")
                    .content("자동 결제 시스템 처리")
                    .userId(event.getUserId())
                    .orderId(event.getOrderId())
                    .paymentMethod(PaymentEntity.PaymentMethod.CREDIT_CARD)
                    .paymentAmount(event.getAmount())
                    .build();
            
            // 2. 가공된 DTO를 서비스에 넘기기
            UUID paymentId = paymentService.ready(confirmRequest);
            
            // 3. 결제 시도 및 결과에 따른 분기 처리
            try {
                paymentService.createPaymentBillingApprove(paymentId);
                log.info("결제 승인 완료: paymentId={}", paymentId);
            } catch (BillingKeyNotFoundException e) {

                AuthRequiredEvent authEvent = AuthRequiredEvent.builder()
                        .orderId(event.getOrderId())
                        .userId(event.getUserId())
                        .message(e.getMessage())
                        .build();
                
                paymentEventProducer.sendAuthRequiredEvent(authEvent);
            }
            
        } catch (Exception e) {
            // 에러 처리 로직
            log.error("주문 이벤트 처리 실패: {}", message, e);
        }
    }
}
