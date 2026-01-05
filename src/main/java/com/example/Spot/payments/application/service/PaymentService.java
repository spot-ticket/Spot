package com.example.Spot.payments.application.service;

import java.util.UUID;

import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto.Confirm confirmPaymentBilling(PaymentRequestDto.Confirm request);
    PaymentResponseDto.Cancel cancelPayment(PaymentRequestDto.Cancel request);
    PaymentResponseDto.PaymentList getAllPayment();
    PaymentResponseDto.PaymentDetail getDetailPayment(UUID paymentId);
    PaymentResponseDto.CancelList getAllPaymentCancel();
    PaymentResponseDto.CancelList getDetailPaymentCancel(UUID paymentId);
}
