package com.example.Spot.order.infrastructure.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.example.Spot.order.infrastructure.event.publish.OrderAcceptedEvent;
import com.example.Spot.order.infrastructure.event.publish.OrderPendingEvent;
import com.example.Spot.order.infrastructure.event.subscribe.AuthRequiredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final ObjectMapper objectMapper;

    // 1. 유저 결제 수단 필요 알림
    @KafkaListener(topics = "${spring.kafka.topic.payment-auth.required}", groupId = "${spring.kafka.consumer.group.customer}")
    public void handleAuthRequired(String message, Acknowledgment ack) {
        parseEvent(message, AuthRequiredEvent.class, ack, event ->
                log.info("[고객알림] 유저 {}: 결제 수단이 없어 주문이 대기 중입니다. 사유: {}",
                        event.getUserId(), event.getMessage())
        );
    }

    // 2. 사장님 새 주문 알림
    @KafkaListener(topics = "${spring.kafka.topic.order.pending}", groupId = "${spring.kafka.consumer.group.owner}")
    public void handleOrderPending(String message, Acknowledgment ack) {
        parseEvent(message, OrderPendingEvent.class, ack, event ->
                log.info("[사장알림] 가게 ID {}: 새 주문이 들어왔습니다! (주문 ID: {})",
                        event.getStoreId(), event.getOrderId())
        );
    }

    // 3. 주문 수락 - 고객용
    @KafkaListener(topics = "${spring.kafka.topic.order.accepted}", groupId = "${spring.kafka.consumer.group.customer}")
    public void handleAcceptedCustomer(String message, Acknowledgment ack) {
        parseEvent(message, OrderAcceptedEvent.class, ack, event ->
                log.info("[고객알림] 유저 {}: 주문이 수락되었습니다. {}분 뒤 도착 예정!",
                        event.getUserId(), event.getEstimatedTime())
        );
    }

    // 4. 주문 수락 - 요리사용
    @KafkaListener(topics = "${spring.kafka.topic.order.accepted}", groupId = "${spring.kafka.consumer.group.chef}")
    public void handleAcceptedChef(String message, Acknowledgment ack) {
        parseEvent(message, OrderAcceptedEvent.class, ack, event ->
                log.info("[주방알림] 주문번호 {}: 조리 시작! (예상시간: {}분)",
                        event.getOrderId(), event.getEstimatedTime())
        );
    }

    // 공통 파싱 및 로직 실행 메서드
    private <T> void parseEvent(String message, Class<T> clazz, Acknowledgment ack, java.util.function.Consumer<T> handler) {
        try {
            T event = objectMapper.readValue(message, clazz);
            handler.accept(event);
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("[{}] 알림 파싱/처리 에러: {}", clazz.getSimpleName(), e.getMessage());
            // 필요 시 여기서 예외를 다시 던져서 Kafka Retry 유도 가능
        }
    }
}
