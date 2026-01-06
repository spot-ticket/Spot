package com.example.Spot.payments.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity extends BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(columnDefinition = "UUID", updatable = false)
  private UUID id;

  @Column(updatable = false, nullable = false, name = "user_id")
  private Integer userId;

  @Column(updatable = false, nullable = false, name = "order_id")
  private UUID orderId;

  @Column(updatable = false, nullable = false, length = 100)
  private String paymentTitle;

  @Column(updatable = false, nullable = false, length = 255)
  private String paymentContent;

  @Enumerated(EnumType.STRING)
  @Column(updatable = false, nullable = false, name = "payment_method")
  private PaymentMethod paymentMethod;

  @Column(updatable = false, nullable = false, name = "payment_amount")
  private Long totalAmount;

  @Builder
  public PaymentEntity(
      Integer userId,
      UUID orderId,
      String title,
      String content,
      PaymentMethod paymentMethod,
      Long totalAmount) {

    if (userId == null) {
      throw new IllegalArgumentException("PaymentEntity에 Order ID를 입력하지 않았습니다.");
    }
    if (orderId == null) {
      throw new IllegalArgumentException("PaymentEntity에 User ID를 입력하지 않았습니다.");
    }

    this.userId = userId;
    this.orderId = orderId;
    this.paymentTitle = title;
    this.paymentContent = content;
    this.paymentMethod = paymentMethod;
    this.totalAmount = totalAmount;
  }

  public enum PaymentMethod {
    CREDIT_CARD, // 신용 카드
    BANK_TRANSFER // 계좌 이체
  }
}
