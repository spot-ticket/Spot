package com.example.Spot.payments.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Spot.payments.domain.entity.UserBillingAuthEntity;

@Repository
public interface UserBillingAuthRepository extends JpaRepository<UserBillingAuthEntity, UUID> {

  @Query("SELECT uba FROM UserBillingAuthEntity uba " +
         "WHERE uba.userId = :userId AND uba.isActive = true")
  Optional<UserBillingAuthEntity> findActiveByUserId(@Param("userId") Integer userId);

  boolean existsByUserIdAndIsActiveTrue(Integer userId);
}
