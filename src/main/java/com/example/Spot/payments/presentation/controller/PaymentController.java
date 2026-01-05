package com.example.Spot.payments.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.payments.application.service.PaymentService;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ApiResponse<PaymentResponseDto.Confirm> paymentBillingConfirm(
        PaymentRequestDto.Confirm request
    ) {
        PaymentResponseDto.Confirm response = paymentService.paymentBillingConfirm(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }

}
