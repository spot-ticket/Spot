package com.example.Spot.payments.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "p_user_billing_auth")
public class UserBillingAuthEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false)
  private Integer userId;

  @Column(nullable = false)
  private String authKey;

  @Column(nullable = false)
  private String customerKey;

  @Column
  private String billingKey;

  @Column(nullable = false)
  private LocalDateTime issuedAt;

  @Column(nullable = false)
  private Boolean isActive;

  public void deactivate() {
    this.isActive = false;
  }

  public void updateBillingKey(String billingKey) {
    this.billingKey = billingKey;
  }

  @Builder
  public UserBillingAuthEntity (Integer userId, String authKey, String customerKey, String billingKey, LocalDateTime issuedAt) {

    this.userId = userId;
    this.authKey = authKey;
    this.customerKey = customerKey;
    this.billingKey = billingKey;
    this.issuedAt = issuedAt;

    this.isActive = true;
  }
}
