package com.example.Spot.payments.application.service;

import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;

public interface PaymentService {

    PaymentResponseDto.Confirm paymentBillingConfirm(PaymentRequestDto.Confirm request);
    PaymentResponseDto.Cancel paymentCancel(PaymentRequestDto.Cancel request);
    PaymentResponseDto.View paymentUserView();
}
