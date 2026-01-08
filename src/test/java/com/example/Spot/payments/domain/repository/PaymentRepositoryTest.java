package com.example.Spot.payments.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.Spot.config.TestConfig;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity.PaymentStatus;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class PaymentRepositoryTest {

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private PaymentHistoryRepository paymentHistoryRepository;

  private UUID orderId;
  private Integer userId;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    userId = 1;
  }

  @Nested
  @DisplayName("findActivePaymentByOrderId 테스트 - 멱등성 체크용")
  class FindActivePaymentByOrderIdTest {

    @Test
    @DisplayName("정상: READY 상태의 결제가 있으면 조회된다")
    void READY_상태_결제_조회_테스트() {
      // given
      PaymentEntity payment = createAndSavePayment(orderId);
      createAndSaveHistory(payment.getId(), PaymentStatus.READY);

      // when
      Optional<PaymentEntity> result = paymentRepository.findActivePaymentByOrderId(orderId);

      // then
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("정상: IN_PROGRESS 상태의 결제가 있으면 조회된다")
    void IN_PROGRESS_상태_결제_조회_테스트() {
      // given
      PaymentEntity payment = createAndSavePayment(orderId);
      createAndSaveHistory(payment.getId(), PaymentStatus.READY);
      createAndSaveHistory(payment.getId(), PaymentStatus.IN_PROGRESS);

      // when
      Optional<PaymentEntity> result = paymentRepository.findActivePaymentByOrderId(orderId);

      // then
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("정상: DONE 상태의 결제가 있으면 조회된다 (이미 결제 완료)")
    void DONE_상태_결제_조회_테스트() {
      // given
      PaymentEntity payment = createAndSavePayment(orderId);
      createAndSaveHistory(payment.getId(), PaymentStatus.READY);
      createAndSaveHistory(payment.getId(), PaymentStatus.IN_PROGRESS);
      createAndSaveHistory(payment.getId(), PaymentStatus.DONE);

      // when
      Optional<PaymentEntity> result = paymentRepository.findActivePaymentByOrderId(orderId);

      // then
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("정상: ABORTED 상태의 결제만 있으면 조회되지 않는다 (재시도 가능)")
    void ABORTED_상태_결제_미조회_테스트() {
      // given
      PaymentEntity payment = createAndSavePayment(orderId);
      createAndSaveHistory(payment.getId(), PaymentStatus.READY);
      createAndSaveHistory(payment.getId(), PaymentStatus.IN_PROGRESS);
      createAndSaveHistory(payment.getId(), PaymentStatus.ABORTED);

      // when
      Optional<PaymentEntity> result = paymentRepository.findActivePaymentByOrderId(orderId);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상: CANCELLED 상태의 결제만 있으면 조회되지 않는다 (재결제 가능)")
    void CANCELLED_상태_결제_미조회_테스트() {
      // given
      PaymentEntity payment = createAndSavePayment(orderId);
      createAndSaveHistory(payment.getId(), PaymentStatus.READY);
      createAndSaveHistory(payment.getId(), PaymentStatus.IN_PROGRESS);
      createAndSaveHistory(payment.getId(), PaymentStatus.DONE);
      createAndSaveHistory(payment.getId(), PaymentStatus.CANCELLED_IN_PROGRESS);
      createAndSaveHistory(payment.getId(), PaymentStatus.CANCELLED);

      // when
      Optional<PaymentEntity> result = paymentRepository.findActivePaymentByOrderId(orderId);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상: 해당 orderId로 결제가 없으면 빈 Optional을 반환한다")
    void 결제_없음_테스트() {
      // given
      UUID nonExistentOrderId = UUID.randomUUID();

      // when
      Optional<PaymentEntity> result =
          paymentRepository.findActivePaymentByOrderId(nonExistentOrderId);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상: 다른 orderId의 결제는 조회되지 않는다")
    void 다른_주문_결제_미조회_테스트() {
      // given
      UUID anotherOrderId = UUID.randomUUID();
      PaymentEntity payment = createAndSavePayment(anotherOrderId);
      createAndSaveHistory(payment.getId(), PaymentStatus.READY);

      // when
      Optional<PaymentEntity> result = paymentRepository.findActivePaymentByOrderId(orderId);

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllPaymentsWithLatestStatus 테스트")
  class FindAllPaymentsWithLatestStatusTest {

    @Test
    @DisplayName("정상: 모든 결제와 최신 상태를 함께 조회한다")
    void 전체_결제_조회_테스트() {
      // given
      PaymentEntity payment1 = createAndSavePayment(UUID.randomUUID());
      createAndSaveHistory(payment1.getId(), PaymentStatus.READY);
      createAndSaveHistory(payment1.getId(), PaymentStatus.IN_PROGRESS);
      createAndSaveHistory(payment1.getId(), PaymentStatus.DONE);

      PaymentEntity payment2 = createAndSavePayment(UUID.randomUUID());
      createAndSaveHistory(payment2.getId(), PaymentStatus.READY);

      // when
      var results = paymentRepository.findAllPaymentsWithLatestStatus();

      // then
      assertThat(results).hasSize(2);
    }
  }

  @Nested
  @DisplayName("findPaymentWithLatestStatus 테스트")
  class FindPaymentWithLatestStatusTest {

    @Test
    @DisplayName("정상: 특정 결제의 최신 상태를 조회한다")
    void 단건_결제_조회_테스트() {
      // given
      PaymentEntity payment = createAndSavePayment(orderId);
      createAndSaveHistory(payment.getId(), PaymentStatus.READY);
      createAndSaveHistory(payment.getId(), PaymentStatus.IN_PROGRESS);
      createAndSaveHistory(payment.getId(), PaymentStatus.DONE);

      // when
      var results = paymentRepository.findPaymentWithLatestStatus(payment.getId());

      // then
      assertThat(results).hasSize(1);
      assertThat(results.get(0)[5]).isEqualTo(PaymentStatus.DONE);
    }

    @Test
    @DisplayName("정상: 존재하지 않는 결제 ID로 조회하면 빈 리스트를 반환한다")
    void 없는_결제_조회_테스트() {
      // given
      UUID nonExistentPaymentId = UUID.randomUUID();

      // when
      var results = paymentRepository.findPaymentWithLatestStatus(nonExistentPaymentId);

      // then
      assertThat(results).isEmpty();
    }
  }

  private PaymentEntity createAndSavePayment(UUID orderId) {
    PaymentEntity payment =
        PaymentEntity.builder()
            .userId(userId)
            .orderId(orderId)
            .title("테스트 결제")
            .content("테스트 내용")
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .totalAmount(10000L)
            .build();
    return paymentRepository.save(payment);
  }

  private PaymentHistoryEntity createAndSaveHistory(UUID paymentId, PaymentStatus status) {
    PaymentHistoryEntity history =
        PaymentHistoryEntity.builder().paymentId(paymentId).status(status).build();
    return paymentHistoryRepository.save(history);
  }
}
