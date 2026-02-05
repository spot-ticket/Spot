package com.example.Spot.payments.infrastructure.aop;

import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
import com.example.Spot.payments.application.service.PaymentHistoryService;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PaymentAspect {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryService paymentHistoryService;

    @Pointcut("execution(* com.example.Spot.payments.application.service.PaymentService.*(..))")
    private void paymentServiceMethods() {}

    @Before("paymentServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("[Payment 로직] 메서드 실행 시작: " + methodName);
    }

    @Around("@annotation(ready)")
    public Object handleReady(ProceedingJoinPoint joinPoint, Ready ready) throws Throwable {

        PaymentRequestDto.Confirm request = (PaymentRequestDto.Confirm) joinPoint.getArgs()[2];

        validatePaymentRequest(request);

        // 멱등성 체크: 동일 주문에 대해 진행 중이거나 완료된 결제가 있는지 확인
        paymentRepository
            .findActivePaymentByOrderId(request.orderId())
            .ifPresent(existingPayment -> {
                throw new IllegalStateException(
                    "[PaymentService] 이미 해당 주문에 대한 결제가 진행 중이거나 완료되었습니다. paymentId: " + existingPayment.getId()
                );
            });

        try {
            Object result = joinPoint.proceed();
            
            UUID paymentId = (UUID) result;

            paymentHistoryService.recordPaymentReady(paymentId);

            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @Around("@annotation(paymentBillingApproveTrace)")
    public Object handlePaymentBillingApproveStatus(ProceedingJoinPoint joinPoint, PaymentBillingApproveTrace trace) throws Throwable {

        UUID paymentId = (UUID) joinPoint.getArgs()[0];
        log.info("📢 [AOP] 결제 승인 기록 시작 - ID: {}", paymentId); // 로그 확인용
        
        try {
            paymentHistoryService.recordPaymentProgress(paymentId);

            Object result = joinPoint.proceed();

            if (result instanceof PaymentResponseDto.Confirm) {
                PaymentResponseDto.Confirm response = (PaymentResponseDto.Confirm) result;
                String paymentKey = response.paymentKey();
                log.info("✅ [AOP] 결제 성공 기록 완료");
                
                paymentHistoryService.recordPaymentSuccess(paymentId, paymentKey);
            }

            return result;

        } catch (Exception e) {
            log.error("❌ [AOP] 에러 발생: {}", e.getMessage());
            paymentHistoryService.recordFailure(paymentId, e);
            throw e;
        }
    }
    
    private void validatePaymentRequest(PaymentRequestDto.Confirm request) {
        
        if (request.paymentAmount() <= 0) {
            throw new IllegalArgumentException("[PaymentService] 결제 금액은 0보다 커야 합니다");
        }
    }

    @Around("@annotation(cancelTrace)")
    public Object handleCancelStatus(ProceedingJoinPoint jointPoint, Cancel cancel) throws Throwable {

        PaymentRequestDto.Cancel request = (PaymentRequestDto.Cancel) jointPoint.getArgs()[0];

        paymentHistoryService.recordCancelProgress(request.paymentId());

        try {

            Object result = jointPoint.proceed();

            paymentHistoryService.recordCancelSuccess(request.paymentId());

            return result;

        } catch (Exception e) {
            paymentHistoryService.recordFailure(request.paymentId(), e);
            throw new RuntimeException("[PaymentService] 결제 취소 실패: " + e.getMessage(), e);
        }
    }

    private PaymentEntity findPayment(UUID paymentId) {
        return paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("[PaymentService] 결제를 찾을 수 없습니다."));
  }
}
