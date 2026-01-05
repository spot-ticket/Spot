package com.example.Spot.payments.presentation.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.payments.application.service.PaymentService;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ApiResponse<PaymentResponseDto.Confirm> confirmPayment(
        @Valid @RequestBody PaymentRequestDto.Confirm request
    ) {
        PaymentResponseDto.Confirm response = paymentService.confirmPaymentBilling(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @PostMapping("/cancel")
    public ApiResponse<PaymentResponseDto.Cancel> cancelPayment(
        @Valid @RequestBody PaymentRequestDto.Cancel request
    ) {
        PaymentResponseDto.Cancel response = paymentService.cancelPayment(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping
    public ApiResponse<PaymentResponseDto.PaymentList> getAllPayment() {
        PaymentResponseDto.PaymentList response = paymentService.getAllPayment();
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponseDto.PaymentDetail> getDetailPayment(
        @PathVariable UUID paymentId
    ) {
        PaymentResponseDto.PaymentDetail response = paymentService.getDetailPayment(paymentId);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping("/cancel")
    public ApiResponse<PaymentResponseDto.CancelList> getAllPaymentCancel() {
        PaymentResponseDto.CancelList response = paymentService.getAllPaymentCancel();
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

    @GetMapping("/{paymentId}/cancel")
    public ApiResponse<PaymentResponseDto.CancelList> getDetailPaymentCancel(
        @PathVariable UUID paymentId
    ) {
        PaymentResponseDto.CancelList response = paymentService.getDetailPaymentCancel(paymentId);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

}
