package com.example.Spot.internal.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.internal.dto.InternalPaymentCancelRequest;
import com.example.Spot.internal.dto.InternalPaymentResponse;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;
import com.example.Spot.payments.domain.gateway.PaymentGateway;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentKeyRepository paymentKeyRepository;
    private final PaymentGateway paymentGateway;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<InternalPaymentResponse> getPaymentByOrderId(@PathVariable UUID orderId) {
        Optional<PaymentEntity> paymentOpt = paymentRepository.findActivePaymentByOrderId(orderId);

        if (paymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PaymentEntity payment = paymentOpt.get();
        Optional<PaymentKeyEntity> paymentKeyOpt = paymentKeyRepository.findByPaymentId(payment.getId());

        return ResponseEntity.ok(InternalPaymentResponse.from(payment, paymentKeyOpt.orElse(null)));
    }

    @GetMapping("/order/{orderId}/exists")
    public ResponseEntity<Boolean> existsActivePaymentByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentRepository.findActivePaymentByOrderId(orderId).isPresent());
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable UUID paymentId,
            @RequestBody InternalPaymentCancelRequest request) {

        if (!paymentRepository.existsById(paymentId)) {
            throw new IllegalArgumentException("결제를 찾을 수 없습니다.");
        }

        Optional<PaymentKeyEntity> paymentKeyOpt = paymentKeyRepository.findByPaymentId(paymentId);

        if (paymentKeyOpt.isEmpty()) {
            log.warn("결제 ID {}에 대한 결제 키가 없습니다.", paymentId);
            return ResponseEntity.ok().build();
        }

        PaymentKeyEntity paymentKey = paymentKeyOpt.get();

        try {
            paymentGateway.cancelPayment(paymentKey.getPaymentKey(), request.getCancelReason(), 10);
            log.info("결제 취소 완료 - 결제 ID: {}", paymentId);
        } catch (Exception e) {
            log.error("결제 취소 실패 - 결제 ID: {}, 오류: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("결제 취소 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }
}
