package com.example.Spot.order.infrastructure.aop;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.Spot.global.feign.MenuClient;
import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.dto.MenuOptionResponse;
import com.example.Spot.global.feign.dto.MenuResponse;
import com.example.Spot.global.feign.dto.StoreResponse;
import com.example.Spot.global.feign.dto.StoreUserResponse;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.order.infrastructure.aop.OrderValidationContext.ContextData;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemOptionRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemRequestDto;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OrderAspect {

    private final StoreClient storeClient;
    private final MenuClient menuClient;
    private final OrderRepository orderRepository;

    @Around("@annotation(validateStoreAndMenu)")
    @CircuitBreaker(name = "store_menus_validation")
    @Bulkhead(name = "store_menus_validation", type = Bulkhead.Type.SEMAPHORE)
    @Retry(name = "store_menus_validation")
    public Object handleValidateStoreAndMenu(
            ProceedingJoinPoint joinPoint,
            ValidateStoreAndMenu validateStoreAndMenu) throws Throwable {

        try {
            OrderCreateRequestDto requestDto = (OrderCreateRequestDto) joinPoint.getArgs()[0];
            ContextData contextData = new ContextData();

            // Store 서비스에서 가게 정보 조회
            StoreResponse store = storeClient.getStoreById(requestDto.getStoreId());
            if (store == null) {
                throw new IllegalArgumentException("존재하지 않는 가게입니다.");
            }
            contextData.setStoreResponse(store);

            for (OrderItemRequestDto itemDto : requestDto.getOrderItems()) {
                // Menu 서비스에서 메뉴 정보 조회
                MenuResponse menu = menuClient.getMenuById(itemDto.getMenuId());
                if (menu == null) {
                    throw new IllegalArgumentException("존재하지 않는 메뉴입니다: " + itemDto.getMenuId());
                }

                if (menu.isHidden() || menu.isDeleted()) {
                    throw new IllegalArgumentException("판매 중지된 메뉴입니다: " + menu.getName());
                }

                contextData.addMenuResponse(itemDto.getMenuId(), menu);

                for (OrderItemOptionRequestDto optionDto : itemDto.getOptions()) {
                    MenuOptionResponse menuOption = menuClient.getMenuOptionById(optionDto.getMenuOptionId());
                    if (menuOption == null) {
                        throw new IllegalArgumentException("존재하지 않는 옵션입니다: " + optionDto.getMenuOptionId());
                    }

                    if (menuOption.isDeleted()) {
                        throw new IllegalArgumentException("선택할 수 없는 옵션입니다: " + menuOption.getName());
                    }

                    contextData.addMenuOptionResponse(optionDto.getMenuOptionId(), menuOption);
                }
            }

            OrderValidationContext.set(contextData);

            return joinPoint.proceed();

        } finally {
            OrderValidationContext.clear();
        }
    }

    @Around("@annotation(orderStatusChange)")
    public Object handleOrderStatusChange(
            ProceedingJoinPoint joinPoint,
            OrderStatusChange orderStatusChange) throws Throwable {

        UUID orderId = (UUID) joinPoint.getArgs()[0];
        String statusType = orderStatusChange.value();
        String methodName = joinPoint.getSignature().getName();

        log.info("[주문 상태 변경] 시작 - OrderId: {}, Type: {}, Method: {}", orderId, statusType, methodName);

        try {
            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

            OrderValidationContext.setCurrentOrder(order);

            Object result = joinPoint.proceed();

            log.info("[주문 상태 변경] 완료 - OrderId: {}, Type: {}, NewStatus: {}",
                    orderId, statusType, order.getOrderStatus());

            return result;

        } catch (Exception e) {
            log.error("[주문 상태 변경] 실패 - OrderId: {}, Type: {}, Error: {}",
                    orderId, statusType, e.getMessage(), e);
            throw e;
        } finally {
            OrderValidationContext.clearCurrentOrder();
        }
    }

    @Around("@annotation(paymentCancelTrace)")
    public Object handlePaymentCancelTrace(
            ProceedingJoinPoint joinPoint,
            PaymentCancelTrace paymentCancelTrace) throws Throwable {

        UUID orderId = (UUID) joinPoint.getArgs()[0];
        String cancelReason = (String) joinPoint.getArgs()[1];

        log.info("[결제 취소] 시작 - OrderId: {}, Reason: {}", orderId, cancelReason);

        try {
            Object result = joinPoint.proceed();
            log.info("[결제 취소] 완료 - OrderId: {}", orderId);
            return result;

        } catch (Exception e) {
            log.error("[결제 취소] 실패 - OrderId: {}, Error: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Around("@annotation(storeOwnershipRequired)")
    @CircuitBreaker(name = "store_me_ownership")
    @Bulkhead(name = "store_me_ownership", type = Bulkhead.Type.SEMAPHORE)
    @Retry(name = "store_me_ownership")
    public Object handleStoreOwnershipRequired(
            ProceedingJoinPoint joinPoint,
            StoreOwnershipRequired storeOwnershipRequired) throws Throwable {

        Integer userId = (Integer) joinPoint.getArgs()[0];

        log.debug("[가게 소유권 검증] UserId: {}", userId);

        // Store 서비스에서 StoreUser 정보 조회
        StoreUserResponse storeUser = storeClient.getStoreUserByUserId(userId);
        if (storeUser == null) {
            throw new IllegalArgumentException("소속된 매장이 없습니다.");
        }

        UUID storeId = storeUser.getStoreId();

        // ThreadLocal에 storeId 저장 (서비스에서 재조회 방지)
        OrderValidationContext.setCurrentStoreId(storeId);

        try {
            return joinPoint.proceed();
        } finally {
            OrderValidationContext.clearCurrentStoreId();
        }
    }
}
