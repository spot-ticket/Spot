package com.example.Spot.payments.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  // private final PaymentService paymentService;

  // @PostMapping("/confirm")
  // public ApiResponse<PaymentResponseDto.Confirm> confirmPayment(
  //     @Valid @RequestBody PaymentRequestDto.Confirm request
  // ) {
  //     PaymentResponseDto.Confirm response = paymentService.confirmPaymentBilling(request);
  //     return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
  // }

  // @PostMapping("/cancel")
  // public ApiResponse<PaymentResponseDto.Cancel> cancelPayment(
  //     @Valid @RequestBody PaymentRequestDto.Cancel request
  // ) {
  //     PaymentResponseDto.Cancel response = paymentService.cancelPayment(request);
  //     return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
  // }

  // @GetMapping
  // public ApiResponse<PaymentResponseDto.PaymentList> getAllPayment() {
  //     PaymentResponseDto.PaymentList response = paymentService.getAllPayment();
  //     return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
  // }

  // @GetMapping("/{paymentId}")
  // public ApiResponse<PaymentResponseDto.PaymentDetail> getDetailPayment(
  //     @PathVariable UUID paymentId
  // ) {
  //     PaymentResponseDto.PaymentDetail response = paymentService.getDetailPayment(paymentId);
  //     return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
  // }

  // @GetMapping("/cancel")
  // public ApiResponse<PaymentResponseDto.CancelList> getAllPaymentCancel() {
  //     PaymentResponseDto.CancelList response = paymentService.getAllPaymentCancel();
  //     return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
  // }

  // @GetMapping("/{paymentId}/cancel")
  // public ApiResponse<PaymentResponseDto.CancelList> getDetailPaymentCancel(
  //     @PathVariable UUID paymentId
  // ) {
  //     PaymentResponseDto.CancelList response = paymentService.getDetailPaymentCancel(paymentId);
  //     return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
  // }
}
