package com.example.Spot.payments.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // 알 수 없는 필드 무시
public class TossPaymentResponse {

  // 일반 결제 응답 필드
  private String mId;
  private String lastTransactionKey;
  private String paymentKey;
  private String orderId;
  private String orderName;
  private Long taxExemptionAmount;
  private String status;
  private String requestedAt;
  private String approvedAt;
  private Boolean useEscrow;
  private Boolean cultureExpense;
  private String type;
  private String country;
  private String currency;
  private Long totalAmount;
  private Long balanceAmount;
  private Long suppliedAmount;
  private Long vat;
  private Long taxFreeAmount;
  private String method;
  private String version;
  private String secret;
  private Boolean isPartialCancelable;

  // 빌링키 발급 응답 필드
  private String billingKey;
  private String customerKey;
  private String authenticatedAt;

  // 카드 정보
  private CardInfo card;

  // Receipt 정보
  private ReceiptInfo receipt;

  // Checkout 정보
  private CheckoutInfo checkout;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CardInfo {
    private String issuerCode;
    private String acquirerCode;
    private String number;
    private Integer installmentPlanMonths;
    private Boolean isInterestFree;
    private String approveNo;
    private Boolean useCardPoint;
    private String cardType;
    private String ownerType;
    private String acquireStatus;
    private Long amount;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ReceiptInfo {
    private String url;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CheckoutInfo {
    private String url;
  }
}
