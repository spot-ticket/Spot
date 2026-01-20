package com.example.Spot.payments.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.Spot.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "p_payment_retry")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentRetryEntity extends BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(columnDefinition = "UUID")
  private UUID id;

  @Column(name = "payment_id", nullable = false, updatable = false, columnDefinition = "UUID")
  private UUID paymentId;

  @Column(
      name = "failed_payment_item_id",
      nullable = false,
      updatable = false,
      columnDefinition = "UUID")
  private UUID failedPaymentHistoryId; // PaymentHistory 테이블 참조

  @Column(nullable = false)
  private Integer attemptCount;

  @Column(nullable = false)
  private Integer maxRetryCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RetryStatus status;

  @Column(nullable = false)
  private LocalDateTime nextRetryAt;

  @Column(length = 1000)
  private String lastErrorMessage;

  @Column(length = 500)
  private String lastErrorCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RetryStrategy strategy;

  @Builder
  public PaymentRetryEntity(
      UUID paymentId,
      UUID failedPaymentHistoryId,
      Integer maxRetryCount,
      RetryStrategy strategy,
      LocalDateTime nextRetryAt) {
    if (paymentId == null) {
      throw new IllegalArgumentException("결제 ID는 필수입니다");
    }
    if (failedPaymentHistoryId == null) {
      throw new IllegalArgumentException("실패한 결제 항목 ID는 필수입니다");
    }
    if (maxRetryCount == null || maxRetryCount < 1) {
      throw new IllegalArgumentException("최대 재시도 횟수는 1 이상이어야 합니다");
    }

    this.paymentId = paymentId;
    this.failedPaymentHistoryId = failedPaymentHistoryId;
    this.attemptCount = 0;
    this.maxRetryCount = maxRetryCount;
    this.status = RetryStatus.PENDING;
    this.strategy = strategy != null ? strategy : RetryStrategy.EXPONENTIAL_BACKOFF;
    this.nextRetryAt = nextRetryAt != null ? nextRetryAt : calculateNextRetryTime(0, this.strategy);
  }

  public void recordFailedAttempt(String errorMessage, String errorCode) {
    this.attemptCount++;
    this.lastErrorMessage = errorMessage;
    this.lastErrorCode = errorCode;

    if (this.attemptCount >= this.maxRetryCount) {
      this.status = RetryStatus.EXHAUSTED;
    } else {
      this.nextRetryAt = calculateNextRetryTime(this.attemptCount, this.strategy);
      this.status = RetryStatus.PENDING;
    }
  }

  public void markAsSucceeded() {
    this.status = RetryStatus.SUCCEEDED;
  }

  public void markAsAbandoned(String reason) {
    this.status = RetryStatus.ABANDONED;
    this.lastErrorMessage = reason;
  }

  public void markAsInProgress() {
    this.status = RetryStatus.IN_PROGRESS;
  }

  public boolean canRetry() {
    return this.status == RetryStatus.PENDING
        && this.attemptCount < this.maxRetryCount
        && LocalDateTime.now().isAfter(this.nextRetryAt);
  }

  private LocalDateTime calculateNextRetryTime(int attemptNumber, RetryStrategy strategy) {
    return switch (strategy) {
      case FIXED_INTERVAL -> LocalDateTime.now().plusMinutes(5);
      case LINEAR_BACKOFF -> LocalDateTime.now().plusMinutes(5L * (attemptNumber + 1));
      case EXPONENTIAL_BACKOFF -> {
        long delayMinutes = (long) Math.pow(2, attemptNumber) * 5; // 5, 10, 20, 40분...
        yield LocalDateTime.now().plusMinutes(Math.min(delayMinutes, 120)); // 최대 2시간
      }
      case CUSTOM -> LocalDateTime.now().plusMinutes(10);
    };
  }

  public enum RetryStatus {
    PENDING, // 재시도 대기 중
    IN_PROGRESS, // 재시도 진행 중
    SUCCEEDED, // 재시도 성공
    EXHAUSTED, // 최대 횟수 초과
    ABANDONED // 수동으로 중단됨
  }

  public enum RetryStrategy {
    FIXED_INTERVAL, // 고정 간격 (5분마다)
    LINEAR_BACKOFF, // 선형 증가 (5, 10, 15, 20분...)
    EXPONENTIAL_BACKOFF, // 지수 증가 (5, 10, 20, 40분...)
    CUSTOM // 커스텀 전략
  }
}
