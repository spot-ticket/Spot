package com.example.spotorder.order.infrastructure.aop;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.order.infrastructure.aop.OrderStatusChange;
import com.example.Spot.order.infrastructure.aop.OrderValidationContext;
import com.example.Spot.order.infrastructure.aop.OrderValidationContext.ContextData;
import com.example.Spot.order.infrastructure.aop.PaymentCancelTrace;
import com.example.Spot.order.infrastructure.aop.ValidateStoreAndMenu;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemOptionRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderItemRequestDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.entity.StoreUserEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.domain.repository.StoreUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OrderAspect {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final OrderRepository orderRepository;
    private final StoreUserRepository storeUserRepository;

    @Around("@annotation(validateStoreAndMenu)")
    public Object handleValidateStoreAndMenu(
            ProceedingJoinPoint joinPoint,
            ValidateStoreAndMenu validateStoreAndMenu) throws Throwable {

        try {
            OrderCreateRequestDto requestDto = (OrderCreateRequestDto) joinPoint.getArgs()[0];
            ContextData contextData = new ContextData();

            StoreEntity store = storeRepository.findById(requestDto.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));
            contextData.setStore(store);

            for (OrderItemRequestDto itemDto : requestDto.getOrderItems()) {
                MenuEntity menu = menuRepository.findById(itemDto.getMenuId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 메뉴입니다: " + itemDto.getMenuId()));

                if (!menu.getIsAvailable()) {
                    throw new IllegalArgumentException("판매 중지된 메뉴입니다: " + menu.getName());
                }

                contextData.addMenu(itemDto.getMenuId(), menu);

                for (OrderItemOptionRequestDto optionDto : itemDto.getOptions()) {
                    MenuOptionEntity menuOption = menuOptionRepository.findById(optionDto.getMenuOptionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "존재하지 않는 옵션입니다: " + optionDto.getMenuOptionId()));

                    if (!menuOption.isAvailable()) {
                        throw new IllegalArgumentException(
                                "선택할 수 없는 옵션입니다: " + menuOption.getName());
                    }

                    contextData.addMenuOption(optionDto.getMenuOptionId(), menuOption);
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

            // TODO: 향후 이벤트 발행 (알림, 웹소켓 등)
            // eventPublisher.publishOrderStatusChangedEvent(order, statusType);

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
    public Object handleStoreOwnershipRequired(
            ProceedingJoinPoint joinPoint,
            com.example.Spot.order.infrastructure.aop.StoreOwnershipRequired storeOwnershipRequired) throws Throwable {

        Integer userId = (Integer) joinPoint.getArgs()[0];

        log.debug("[가게 소유권 검증] UserId: {}", userId);

        StoreUserEntity storeUser = storeUserRepository.findFirstByUserId(userId);
        if (storeUser == null) {
            throw new IllegalArgumentException("소속된 매장이 없습니다.");
        }

        UUID storeId = storeUser.getStore().getId();

        // ThreadLocal에 storeId 저장 (서비스에서 재조회 방지)
        OrderValidationContext.setCurrentStoreId(storeId);

        try {
            return joinPoint.proceed();
        } finally {
            OrderValidationContext.clearCurrentStoreId();
        }
    }
}
