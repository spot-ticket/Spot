package com.example.Spot.global.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Spot.global.feign.dto.PaymentCancelRequest;
import com.example.Spot.global.feign.dto.PaymentResponse;

@FeignClient(name = "payment-service", url = "${feign.payment.url}")
public interface PaymentClient {

    @GetMapping("/api/internal/payments/order/{orderId}")
    PaymentResponse getPaymentByOrderId(@PathVariable("orderId") UUID orderId);

    @PostMapping("/api/internal/payments/{paymentId}/cancel")
    void cancelPayment(@PathVariable("paymentId") UUID paymentId, @RequestBody PaymentCancelRequest request);

    @GetMapping("/api/internal/payments/order/{orderId}/exists")
    boolean existsActivePaymentByOrderId(@PathVariable("orderId") UUID orderId);
}
