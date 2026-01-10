package com.example.Spot.payments.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class PaymentResponseDto {

  @Builder
  @Schema(description = "결제 승인 응답")
  public record Confirm(
      @Schema(description = "결제 항목 ID", example = "123e4567-e89b-12d3-a456-426614174000")
          UUID paymentId,
      @Schema(description = "결제 상태", example = "DONE") String status,
      @Schema(description = "결제 금액", example = "18000") Long amount,
      @Schema(description = "승인 시간", example = "2024-01-05T15:30:00") LocalDateTime approvedAt) {}

  @Builder
  @Schema(description = "결제 취소 응답")
  public record Cancel(
      @Schema(description = "결제 ID", example = "123e4567-e89b-12d3-a456-426614174000")
          UUID paymentId,
      @Schema(description = "취소 금액", example = "18000") Long cancelAmount,
      @Schema(description = "취소 사유", example = "고객 요청") String cancelReason,
      @Schema(description = "취소 시간", example = "2024-01-05T16:00:00") LocalDateTime canceledAt) {}

  @Builder
  @Schema(description = "부분 결제 취소 응답")
  public record PartialCancel(
      @Schema(description = "결제 ID", example = "123e4567-e89b-12d3-a456-426614174000")
          UUID paymentId,
      @Schema(description = "취소 금액", example = "18000") Long cancelAmount,
      @Schema(description = "취소 사유", example = "고객 요청") String cancelReason,
      @Schema(description = "취소 시간", example = "2024-01-05T16:00:00") LocalDateTime canceledAt) {}

  @Builder
  @Schema(description = "결제 상세 정보")
  public record PaymentDetail(
      @Schema(description = "결제 ID", example = "123e4567-e89b-12d3-a456-426614174000")
          UUID paymentId,
      @Schema(description = "결제 제목", example = "치킨 주문 결제") String title,
      @Schema(description = "결제 내용", example = "후라이드 치킨 1마리") String content,
      @Schema(description = "결제 방법", example = "CREDIT_CARD") PaymentMethod paymentMethod,
      @Schema(description = "총 결제 금액", example = "18000") Long totalAmount,
      @Schema(description = "결제 상태", example = "DONE") String status,
      @Schema(description = "결제 생성 시간", example = "2024-01-05T15:30:00") LocalDateTime createdAt) {}

  @Builder
  @Schema(description = "결제 목록 응답")
  public record PaymentList(
      @Schema(description = "결제 목록") List<PaymentDetail> payments,
      @Schema(description = "총 결제 수", example = "10") int totalCount) {}

  @Builder
  @Schema(description = "취소 상세 정보")
  public record CancelDetail(
      @Schema(description = "취소 ID", example = "123e4567-e89b-12d3-a456-426614174000")
          UUID cancelId,
      @Schema(description = "결제 ID", example = "123e4567-e89b-12d3-a456-426614174000")
          UUID paymentId,
      @Schema(description = "취소 사유", example = "고객 요청") String cancelReason,
      @Schema(description = "취소 금액", example = "18000") Long cancelAmount,
      @Schema(description = "취소 시간", example = "2024-01-05T16:00:00") LocalDateTime canceledAt) {}

  @Builder
  @Schema(description = "취소 목록 응답")
  public record CancelList(
      @Schema(description = "취소 목록") List<CancelDetail> cancellations,
      @Schema(description = "총 취소 수", example = "5") int totalCount) {}

  @Builder
  @Schema(description = "빌링키 저장 응답")
  public record SavedBillingKey(
      @Schema(description = "사용자 ID", example = "1") Integer userId,
      @Schema(description = "고객 키", example = "customer_1") String customerKey,
      @Schema(description = "빌링키", example = "billing_key_xxxxx") String billingKey,
      @Schema(description = "저장 시간", example = "2024-01-05T15:30:00") LocalDateTime savedAt) {}
}
