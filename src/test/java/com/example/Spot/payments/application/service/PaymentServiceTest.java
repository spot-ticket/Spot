package com.example.Spot.payments.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity.PaymentStatus;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private TossPaymentClient tossPaymentClient;

  @Mock private PaymentRepository paymentRepository;

  @Mock private PaymentHistoryRepository paymentHistoryRepository;

  @Mock private PaymentRetryRepository paymentRetryRepository;

  @Mock private PaymentKeyRepository paymentKeyRepository;

  @Mock private UserRepository userRepository;

  @Mock private OrderRepository orderRepository;

  @InjectMocks private PaymentService paymentService;

  private UUID orderId;
  private Integer userId;
  private PaymentRequestDto.Confirm request;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    userId = 1;
    request =
        PaymentRequestDto.Confirm.builder()
            .title("테스트 결제")
            .content("테스트 내용")
            .userId(userId)
            .orderId(orderId)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .paymentAmount(10000L)
            .build();
  }

  @Nested
  @DisplayName("preparePayment 테스트 - 결제 준비")
  class 결제_준비_테스트 {

    @Test
    @DisplayName("정상: 새로운 결제 요청이 성공한다")
    void 결제_준비_성공_테스트() {
      UserEntity user = createMockUser(userId);
      OrderEntity order = createMockOrder(orderId);
      PaymentEntity payment = createMockPayment(orderId);
      PaymentHistoryEntity history = createMockHistory(payment.getId(), PaymentStatus.READY);

      given(paymentRepository.findActivePaymentByOrderId(orderId)).willReturn(Optional.empty());
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(paymentRepository.save(any(PaymentEntity.class))).willReturn(payment);
      given(paymentHistoryRepository.save(any(PaymentHistoryEntity.class))).willReturn(history);

      UUID resultPaymentId = paymentService.preparePayment(request);

      assertThat(resultPaymentId).isEqualTo(payment.getId());
      verify(paymentRepository, times(1)).findActivePaymentByOrderId(orderId);
      verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
      verify(paymentHistoryRepository, times(1)).save(any(PaymentHistoryEntity.class));
    }

    @Test
    @DisplayName("예외: 결제 금액이 0 이하이면 IllegalArgumentException이 발생한다")
    void 결제_금액_0이하_예외_테스트() {
      PaymentRequestDto.Confirm invalidRequest =
          PaymentRequestDto.Confirm.builder()
              .title("테스트 결제")
              .content("테스트 내용")
              .userId(userId)
              .orderId(orderId)
              .paymentMethod(PaymentMethod.CREDIT_CARD)
              .paymentAmount(0L)
              .build();

      assertThatThrownBy(() -> paymentService.preparePayment(invalidRequest))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("결제 금액은 0보다 커야 합니다");

      verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외: 음수 결제 금액이면 IllegalArgumentException이 발생한다")
    void 음수_결제_금액_예외_테스트() {
      PaymentRequestDto.Confirm invalidRequest =
          PaymentRequestDto.Confirm.builder()
              .title("테스트 결제")
              .content("테스트 내용")
              .userId(userId)
              .orderId(orderId)
              .paymentMethod(PaymentMethod.CREDIT_CARD)
              .paymentAmount(-1000L)
              .build();

      assertThatThrownBy(() -> paymentService.preparePayment(invalidRequest))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("결제 금액은 0보다 커야 합니다");
    }
  }

  @Nested
  @DisplayName("preparePayment 멱등성 테스트 - 중복 결제 방지")
  class 멱등성_테스트 {

    @Test
    @DisplayName("예외: 동일 orderId로 READY 상태의 결제가 있으면 중복 결제가 차단된다")
    void READY_상태_중복_결제_차단_테스트() {
      PaymentEntity existingPayment = createMockPayment(orderId);
      given(paymentRepository.findActivePaymentByOrderId(orderId))
          .willReturn(Optional.of(existingPayment));

      assertThatThrownBy(() -> paymentService.preparePayment(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("이미 해당 주문에 대한 결제가 진행 중이거나 완료되었습니다");

      verify(paymentRepository, never()).save(any());
      verify(paymentHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외: 동일 orderId로 IN_PROGRESS 상태의 결제가 있으면 중복 결제가 차단된다")
    void IN_PROGRESS_상태_중복_결제_차단_테스트() {
      PaymentEntity existingPayment = createMockPayment(orderId);
      given(paymentRepository.findActivePaymentByOrderId(orderId))
          .willReturn(Optional.of(existingPayment));

      assertThatThrownBy(() -> paymentService.preparePayment(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("이미 해당 주문에 대한 결제가 진행 중이거나 완료되었습니다");

      verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("예외: 동일 orderId로 DONE 상태의 결제가 있으면 중복 결제가 차단된다")
    void DONE_상태_중복_결제_차단_테스트() {
      PaymentEntity existingPayment = createMockPayment(orderId);
      given(paymentRepository.findActivePaymentByOrderId(orderId))
          .willReturn(Optional.of(existingPayment));

      assertThatThrownBy(() -> paymentService.preparePayment(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("이미 해당 주문에 대한 결제가 진행 중이거나 완료되었습니다");

      verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상: ABORTED 상태의 결제만 있으면 새 결제가 가능하다 (재시도)")
    void ABORTED_상태_재시도_가능_테스트() {
      UserEntity user = createMockUser(userId);
      OrderEntity order = createMockOrder(orderId);
      PaymentEntity newPayment = createMockPayment(orderId);
      PaymentHistoryEntity history = createMockHistory(newPayment.getId(), PaymentStatus.READY);

      given(paymentRepository.findActivePaymentByOrderId(orderId)).willReturn(Optional.empty());
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(paymentRepository.save(any(PaymentEntity.class))).willReturn(newPayment);
      given(paymentHistoryRepository.save(any(PaymentHistoryEntity.class))).willReturn(history);

      UUID resultPaymentId = paymentService.preparePayment(request);

      assertThat(resultPaymentId).isEqualTo(newPayment.getId());
      verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("정상: CANCELLED 상태의 결제만 있으면 새 결제가 가능하다 (취소 후 재결제)")
    void CANCELLED_상태_재결제_가능_테스트() {
      UserEntity user = createMockUser(userId);
      OrderEntity order = createMockOrder(orderId);
      PaymentEntity newPayment = createMockPayment(orderId);
      PaymentHistoryEntity history = createMockHistory(newPayment.getId(), PaymentStatus.READY);

      given(paymentRepository.findActivePaymentByOrderId(orderId)).willReturn(Optional.empty());
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(paymentRepository.save(any(PaymentEntity.class))).willReturn(newPayment);
      given(paymentHistoryRepository.save(any(PaymentHistoryEntity.class))).willReturn(history);

      UUID resultPaymentId = paymentService.preparePayment(request);

      assertThat(resultPaymentId).isEqualTo(newPayment.getId());
      verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("정상: 다른 orderId의 결제가 진행 중이어도 새 결제가 가능하다")
    void 다른_주문_결제_무관_테스트() {
      UserEntity user = createMockUser(userId);
      OrderEntity order = createMockOrder(orderId);
      PaymentEntity newPayment = createMockPayment(orderId);
      PaymentHistoryEntity history = createMockHistory(newPayment.getId(), PaymentStatus.READY);

      given(paymentRepository.findActivePaymentByOrderId(orderId)).willReturn(Optional.empty());
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(paymentRepository.save(any(PaymentEntity.class))).willReturn(newPayment);
      given(paymentHistoryRepository.save(any(PaymentHistoryEntity.class))).willReturn(history);

      UUID resultPaymentId = paymentService.preparePayment(request);

      assertThat(resultPaymentId).isEqualTo(newPayment.getId());
    }
  }

  @Nested
  @DisplayName("recordPaymentProgress 테스트 - 결제 진행 상태 기록")
  class 결제_진행_상태_기록_테스트 {

    @Test
    @DisplayName("예외: 이미 처리된 결제면 IllegalStateException이 발생한다")
    void 이미_처리된_결제_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentHistoryEntity doneHistory = createMockHistory(paymentId, PaymentStatus.DONE);

      given(paymentHistoryRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId))
          .willReturn(Optional.of(doneHistory));

      assertThatThrownBy(() -> paymentService.executePaymentBilling(paymentId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("이미 처리된 결제입니다");
    }
  }

  private UserEntity createMockUser(Integer userId) {
    UserEntity user =
        UserEntity.builder()
            .username("테스트유저")
            .nickname("테스트닉네임")
            .roadAddress("서울시 강남구")
            .addressDetail("123-45")
            .email("test@test.com")
            .role(null)
            .build();
    ReflectionTestUtils.setField(user, "id", userId);
    return user;
  }

  private OrderEntity createMockOrder(UUID orderId) {
    OrderEntity order = OrderEntity.builder().build();
    ReflectionTestUtils.setField(order, "id", orderId);
    return order;
  }

  private PaymentEntity createMockPayment(UUID orderId) {
    PaymentEntity payment =
        PaymentEntity.builder()
            .userId(userId)
            .orderId(orderId)
            .title("테스트 결제")
            .content("테스트 내용")
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .totalAmount(10000L)
            .build();
    ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
    return payment;
  }

  private PaymentHistoryEntity createMockHistory(UUID paymentId, PaymentStatus status) {
    PaymentHistoryEntity history =
        PaymentHistoryEntity.builder().paymentId(paymentId).status(status).build();
    ReflectionTestUtils.setField(history, "id", UUID.randomUUID());
    return history;
  }
}
