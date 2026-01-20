package com.example.Spot.payments.domain.entity;

import java.time.LocalDateTime;
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
@Table(name = "p_payment_key")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 제한
public class PaymentKeyEntity extends BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(columnDefinition = "UUID", updatable = false)
  private UUID id;

  @Column(nullable = false, updatable = false, name = "payment_id")
  private UUID paymentId; // final 제거

  @Column(nullable = false, updatable = false, name = "payment_key")
  private String paymentKey; // final 제거

  @Column(updatable = false, nullable = false, name = "confirmed_at")
  private LocalDateTime confirmedAt; // final 제거 및 오타 수정

  @Builder
  public PaymentKeyEntity(UUID paymentId, String paymentKey, LocalDateTime confirmedAt) {
    this.paymentId = paymentId;
    this.paymentKey = paymentKey;
    this.confirmedAt = confirmedAt;
  }
}
