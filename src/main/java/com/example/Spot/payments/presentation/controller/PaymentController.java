package com.example.Spot.payments.presentation.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.payments.application.service.PaymentService;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.user.domain.Role;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{order_id}/confirm")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ApiResponse<PaymentResponseDto.Confirm> confirmPayment(
            @PathVariable("order_id") UUID orderId,
            @Valid @RequestBody PaymentRequestDto.Confirm request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        UUID paymentId = paymentService.ready(request);
        PaymentResponseDto.Confirm response = paymentService.createPaymentBillingApprove(paymentId);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @PostMapping("/{order_id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ApiResponse<PaymentResponseDto.Cancel> cancelPayment(
            @PathVariable("order_id") UUID orderId,
            @Valid @RequestBody PaymentRequestDto.Cancel request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        PaymentResponseDto.Cancel response = paymentService.executeCancel(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    public ApiResponse<PaymentResponseDto.PaymentList> getAllPayment() {
        PaymentResponseDto.PaymentList response = paymentService.getAllPayment();
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ApiResponse<PaymentResponseDto.PaymentDetail> getDetailPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        PaymentResponseDto.PaymentDetail response = paymentService.getDetailPayment(paymentId);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping("/cancel")
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    public ApiResponse<PaymentResponseDto.CancelList> getAllPaymentCancel() {
        PaymentResponseDto.CancelList response = paymentService.getAllPaymentCancel();
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping("/{paymentId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ApiResponse<PaymentResponseDto.CancelList> getDetailPaymentCancel(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        PaymentResponseDto.CancelList response = paymentService.getDetailPaymentCancel(paymentId);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @PostMapping("/billing-key")
    public ApiResponse<PaymentResponseDto.SavedBillingKey> saveBillingKey(
            @Valid @RequestBody PaymentRequestDto.SaveBillingKey request
    ) {
        PaymentResponseDto.SavedBillingKey response = paymentService.saveBillingKey(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping("/billing-key/exists")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ApiResponse<Boolean> checkBillingKeyExists(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        boolean exists = paymentService.hasBillingAuth(principal.getUserId());
        System.out.println("빌링키 존재 여부: " + exists + " (UserId: " + principal.getUserId() + ")");
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, exists);
    }
}
