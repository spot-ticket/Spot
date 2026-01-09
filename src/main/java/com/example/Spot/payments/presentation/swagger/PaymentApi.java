package com.example.Spot.payments.presentation.swagger;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "결제", description = "결제 관리 API")
public interface PaymentApi {

    @Operation(summary = "결제 승인", description = "주문에 대한 결제를 승인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 승인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResponse<PaymentResponseDto.Confirm> confirmPayment(
            @Parameter(description = "주문 ID") @PathVariable("order_id") UUID orderId,
            @Valid @RequestBody PaymentRequestDto.Confirm request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "취소 불가능한 상태"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResponse<PaymentResponseDto.Cancel> cancelPayment(
            @Parameter(description = "주문 ID") @PathVariable("order_id") UUID orderId,
            @Valid @RequestBody PaymentRequestDto.Cancel request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "결제 목록 조회", description = "모든 결제 목록을 조회합니다. (관리자 전용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResponse<PaymentResponseDto.PaymentList> getAllPayment();

    @Operation(summary = "결제 상세 조회", description = "특정 결제의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    ApiResponse<PaymentResponseDto.PaymentDetail> getDetailPayment(
            @Parameter(description = "결제 ID") @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "취소 목록 조회", description = "모든 결제 취소 목록을 조회합니다. (관리자 전용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResponse<PaymentResponseDto.CancelList> getAllPaymentCancel();

    @Operation(summary = "결제별 취소 내역 조회", description = "특정 결제의 취소 내역을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    ApiResponse<PaymentResponseDto.CancelList> getDetailPaymentCancel(
            @Parameter(description = "결제 ID") @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails userDetails);
}
