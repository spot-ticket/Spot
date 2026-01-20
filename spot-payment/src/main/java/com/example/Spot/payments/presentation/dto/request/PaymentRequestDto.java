package com.example.Spot.payments.presentation.dto.request;

import java.util.UUID;

import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class PaymentRequestDto {

  @Builder
  @Schema(description = "결제 승인 요청")
  public record Confirm(
      @Schema(description = "결제 제목", example = "치킨 주문 결제") @NotNull String title,
      @Schema(description = "결제 내용", example = "후라이드 치킨 1마리") @NotNull String content,
      @Schema(description = "사용자 ID", example = "1") @NotNull Integer userId,
      @Schema(description = "주문 ID", example = "123e4567-e89b-12d3-a456-426614174000") @NotNull
          UUID orderId,
      @Schema(description = "결제 방법", example = "CREDIT_CARD") @NotNull PaymentMethod paymentMethod,
      @Schema(description = "결제 금액 (원 단위)", example = "18000") @NotNull Long paymentAmount) {}

  @Builder
  @Schema(description = "결제 취소 요청")
  public record Cancel(
      @Schema(description = "결제 ID", example = "123e4567-e89b-12d3-a456-426614174000") @NotNull
          UUID paymentId,
      @Schema(description = "취소 사유", example = "고객 요청") @NotNull String cancelReason) {}

  @Builder
  @Schema(description = "부분 결제 취소 여청")
  public record PartialCancel(
      @Schema(description = "결제 ID", example = "123e4567-e89b-12d3-a456-426614174000") @NotNull
          UUID paymentId,
      @Schema(description = "취소 사유", example = "고객 요청") @NotNull String cancelReason) {}

  @Builder
  @Schema(description = "빌링키 저장 요청")
  public record SaveBillingKey(
      @Schema(description = "사용자 ID", example = "1") @NotNull Integer userId,
      @Schema(description = "고객 키", example = "customer_1") @NotNull String customerKey,
      @Schema(description = "인증 키", example = "authKey_xxxxx") @NotNull String authKey) {}
}
