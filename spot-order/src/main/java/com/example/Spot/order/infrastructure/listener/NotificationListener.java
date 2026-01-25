package com.example.Spot.order.infrastructure.listener;

import com.example.Spot.order.event.publish.OrderPendingEvent;
import com.example.Spot.order.event.subscribe.AuthRequiredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {
    
    private final ObjectMapper objectMapper;
    @KafkaListener(topics = "${kafka.topic.payment-auth.required}", groupId = "notification-group")
    public void handleNotification(String message) {
        try {
            AuthRequiredEvent event = objectMapper.readValue(message, AuthRequiredEvent.class);
            log.info("🔔 [알림] 유저 {}: 결제 수단이 없어 주문이 대기 중입니다. 사유: {}", event.getUserId(), event.getMessage());
        } catch (Exception e) {
            log.error("알림 처리 에러: {}", e.getMessage());
        }
    }
    
    @KafkaListener(topics = "${kafka.topic.order.pending}", groupId = "notification-group")
    public void handleOrderPendingNotification(String message) {
        try {
            OrderPendingEvent event = objectMapper.readValue(message, OrderPendingEvent.class);
            log.info("🔔 [알림] 사장님 가게 ID {}: 새 주문이 들어왔습니다! (주문 ID: {})", 
                    event.getStoreId(), event.getOrderId());
        } catch (Exception e) {
            log.error("사장님 알림 처리 에러: {}", e.getMessage());
        }
    }
}
