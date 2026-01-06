package com.example.Spot.payments.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_payment_cancel")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancelEntity extends BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false, updatable = false, name = "payment_history_id")
  private UUID paymentHistoryId;

  @Column(nullable = false, updatable = false)
  private String reason;

  @Builder
  public PaymentCancelEntity(UUID paymentHistoryId, String reason) {

    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("취소 사유는 필수입니다.");
    }
    if (paymentHistoryId == null) {
      throw new IllegalArgumentException("취소 대상 아이템은 필수입니다.");
    }

    this.paymentHistoryId = paymentHistoryId;
    this.reason = reason;
  }
}
