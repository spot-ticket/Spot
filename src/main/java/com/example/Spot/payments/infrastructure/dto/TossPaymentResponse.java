package com.example.Spot.payments.infrastructure.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentResponse {

  // 일반 결제 응답 필드
  private String paymentKey;
  private String orderId;
  private String orderName;
  private Long amount;
  private String status;
  private LocalDateTime approvedAt;
  private String method;

  // 빌링키 발급 응답 필드
  private String billingKey;
  private String customerKey;

  // 카드 정보
  private CardInfo card;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CardInfo {
    private String company;
    private String number;
    private String cardType;
    private String ownerType;
  }
}
