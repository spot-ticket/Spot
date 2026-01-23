package com.example.Spot.payments.application.service;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.event.subscribe.OrderCreatedEvent;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component // Bean으로 등록되어야 스프링이 Kafka 리스너를 인식합니다.
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService; // AOP가 적용된 대리인(Proxy)을 주입받습니다.

    @KafkaListener(topics = "order-create-topic", groupId = "payment-group")
    public void consumeOrderEvent(OrderCreatedEvent event) {
        log.info(">>>> [Kafka] 주문 이벤트 수신: 주문 ID = {}", event.getOrderId());

        try {
            // 1. 데이터 변환 (Enum 변환 및 DTO 생성)
            String methodString = (event.getPaymentMethod() != null)
                    ? event.getPaymentMethod().toUpperCase()
                    : "CREDIT_CARD";

            // Enum 타입으로 안전하게 변환
            PaymentEntity.PaymentMethod method = PaymentEntity.PaymentMethod.valueOf(methodString);

            PaymentRequestDto.Confirm readyRequest = new PaymentRequestDto.Confirm(
                    event.getTitle(),
                    event.getContent(),
                    event.getUserId(),
                    event.getOrderId(),
                    method,
                    event.getAmount()
            );

            // 2. 서비스 호출 (이제 외부 호출이므로 @Ready, @PaymentBillingApproveTrace AOP가 정상 작동합니다!)
            UUID paymentId = paymentService.ready(readyRequest);
            paymentService.createPaymentBillingApprove(paymentId);

            log.info(">>>> [Kafka] 비동기 결제 프로세스 완료: 결제 ID = {}", paymentId);

        } catch (IllegalArgumentException e) {
            log.error(">>>> [Kafka] 잘못된 결제 수단이 들어왔습니다: {}", event.getPaymentMethod());
        } catch (Exception e) {
            log.error(">>>> [Kafka] 결제 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
