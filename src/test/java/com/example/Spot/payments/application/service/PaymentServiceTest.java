package com.example.Spot.payments.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
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
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity.PaymentStatus;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreUserRepository;
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

  @Mock private StoreUserRepository storeUserRepository;

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
  class PaymentPrepareTest {

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
  class IdempotencyTest {

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
  class PaymentStatusHistoryTest {

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

  @Nested
  @DisplayName("executeCancel 테스트 - 결제 취소")
  class PaymentCancelTest {

    @Test
    @DisplayName("정상: 결제 완료된 건을 취소한다")
    void 결제_취소_성공_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      PaymentHistoryEntity doneHistory = createMockHistory(paymentId, PaymentStatus.DONE);
      PaymentHistoryEntity cancelInProgressHistory = createMockHistory(paymentId, PaymentStatus.CANCELLED_IN_PROGRESS);
      PaymentHistoryEntity cancelledHistory = createMockHistory(paymentId, PaymentStatus.CANCELLED);
      ReflectionTestUtils.setField(cancelledHistory, "createdAt", LocalDateTime.now());

      PaymentKeyEntity paymentKey = PaymentKeyEntity.builder()
          .paymentId(paymentId)
          .paymentKey("test-payment-key")
          .confirmedAt(LocalDateTime.now())
          .build();

      TossPaymentResponse tossResponse = new TossPaymentResponse();
      ReflectionTestUtils.setField(tossResponse, "paymentKey", "test-payment-key");

      PaymentRequestDto.Cancel cancelRequest = PaymentRequestDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelReason("고객 요청")
          .build();

      ReflectionTestUtils.setField(paymentService, "timeout", 30000);

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(paymentHistoryRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId))
          .willReturn(Optional.of(doneHistory));
      given(paymentHistoryRepository.save(any(PaymentHistoryEntity.class)))
          .willReturn(cancelInProgressHistory)
          .willReturn(cancelledHistory);
      given(paymentKeyRepository.findByPaymentId(paymentId)).willReturn(Optional.of(paymentKey));
      given(tossPaymentClient.cancelPayment(anyString(), anyString(), anyInt()))
          .willReturn(tossResponse);

      PaymentResponseDto.Cancel result = paymentService.executeCancel(cancelRequest);

      assertThat(result.paymentId()).isEqualTo(paymentId);
      assertThat(result.cancelReason()).isEqualTo("고객 요청");
      verify(tossPaymentClient, times(1)).cancelPayment(eq("test-payment-key"), eq("고객 요청"), anyInt());
    }

    @Test
    @DisplayName("예외: 결제 완료 상태가 아니면 취소할 수 없다")
    void 결제_완료_아닌_상태_취소_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      PaymentHistoryEntity readyHistory = createMockHistory(paymentId, PaymentStatus.READY);

      PaymentRequestDto.Cancel cancelRequest = PaymentRequestDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelReason("고객 요청")
          .build();

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(paymentHistoryRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId))
          .willReturn(Optional.of(readyHistory));

      assertThatThrownBy(() -> paymentService.executeCancel(cancelRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("결제 완료된 내역만 취소 가능합니다.");
    }

    @Test
    @DisplayName("예외: 결제 키가 없으면 취소할 수 없다")
    void 결제_키_없음_취소_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      PaymentHistoryEntity doneHistory = createMockHistory(paymentId, PaymentStatus.DONE);

      PaymentRequestDto.Cancel cancelRequest = PaymentRequestDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelReason("고객 요청")
          .build();

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(paymentHistoryRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId))
          .willReturn(Optional.of(doneHistory));
      given(paymentHistoryRepository.save(any(PaymentHistoryEntity.class)))
          .willReturn(createMockHistory(paymentId, PaymentStatus.CANCELLED_IN_PROGRESS));
      given(paymentKeyRepository.findByPaymentId(paymentId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> paymentService.executeCancel(cancelRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("결제 키가 없어 취소할 수 없습니다.");
    }
  }

  @Nested
  @DisplayName("validateOwnership 테스트 - 소유권 검증")
  class OwnershipValidationTest {

    @Test
    @DisplayName("정상: 주문 소유자가 일치하면 검증을 통과한다")
    void 주문_소유권_검증_성공_테스트() {
      OrderEntity order = createMockOrder(orderId);

      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

      paymentService.validateOrderOwnership(orderId, userId);

      verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("예외: 주문 소유자가 일치하지 않으면 예외가 발생한다")
    void 주문_소유권_불일치_예외_테스트() {
      OrderEntity order = createMockOrder(orderId);
      Integer otherUserId = 999;

      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

      assertThatThrownBy(() -> paymentService.validateOrderOwnership(orderId, otherUserId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 주문에 대한 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("정상: 결제 소유자가 일치하면 검증을 통과한다")
    void 결제_소유권_검증_성공_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

      paymentService.validatePaymentOwnership(paymentId, userId);

      verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    @DisplayName("예외: 결제 소유자가 일치하지 않으면 예외가 발생한다")
    void 결제_소유권_불일치_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);
      Integer otherUserId = 999;

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

      assertThatThrownBy(() -> paymentService.validatePaymentOwnership(paymentId, otherUserId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 결제에 대한 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("정상: 결제 소유자 ID를 조회한다")
    void 결제_소유자_ID_조회_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

      Integer ownerId = paymentService.getPaymentOwnerId(paymentId);

      assertThat(ownerId).isEqualTo(userId);
    }
  }

  @Nested
  @DisplayName("Store 소유권 검증 테스트")
  class StoreOwnershipValidationTest {

    @Test
    @DisplayName("정상: 가게 소유자가 주문에 접근할 수 있다")
    void 가게_소유자_주문_접근_성공_테스트() {
      OrderEntity order = createMockOrder(orderId);
      UUID storeId = order.getStore().getId();

      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(storeUserRepository.existsByStoreIdAndUserId(storeId, userId)).willReturn(true);

      paymentService.validateOrderStoreOwnership(orderId, userId);

      verify(storeUserRepository, times(1)).existsByStoreIdAndUserId(storeId, userId);
    }

    @Test
    @DisplayName("예외: 가게 소유자가 아니면 주문에 접근할 수 없다")
    void 가게_소유자_아님_주문_접근_예외_테스트() {
      OrderEntity order = createMockOrder(orderId);
      UUID storeId = order.getStore().getId();
      Integer otherUserId = 999;

      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(storeUserRepository.existsByStoreIdAndUserId(storeId, otherUserId)).willReturn(false);

      assertThatThrownBy(() -> paymentService.validateOrderStoreOwnership(orderId, otherUserId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 주문의 가게에 대한 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("정상: 가게 소유자가 결제에 접근할 수 있다")
    void 가게_소유자_결제_접근_성공_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);
      OrderEntity order = createMockOrder(orderId);
      UUID storeId = order.getStore().getId();

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(storeUserRepository.existsByStoreIdAndUserId(storeId, userId)).willReturn(true);

      paymentService.validatePaymentStoreOwnership(paymentId, userId);

      verify(storeUserRepository, times(1)).existsByStoreIdAndUserId(storeId, userId);
    }

    @Test
    @DisplayName("예외: 가게 소유자가 아니면 결제에 접근할 수 없다")
    void 가게_소유자_아님_결제_접근_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);
      OrderEntity order = createMockOrder(orderId);
      UUID storeId = order.getStore().getId();
      Integer otherUserId = 999;

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(storeUserRepository.existsByStoreIdAndUserId(storeId, otherUserId)).willReturn(false);

      assertThatThrownBy(() -> paymentService.validatePaymentStoreOwnership(paymentId, otherUserId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 결제의 가게에 대한 접근 권한이 없습니다.");
    }
  }

  @Nested
  @DisplayName("결제 조회 테스트")
  class PaymentQueryTest {

    @Test
    @DisplayName("정상: 전체 결제 목록을 조회한다")
    void 전체_결제_목록_조회_테스트() {
      UUID paymentId1 = UUID.randomUUID();
      UUID paymentId2 = UUID.randomUUID();
      LocalDateTime now = LocalDateTime.now();

      List<Object[]> mockResults = java.util.Arrays.asList(
          new Object[]{paymentId1, "결제1", "내용1", PaymentMethod.CREDIT_CARD, 10000L, PaymentStatus.DONE, now},
          new Object[]{paymentId2, "결제2", "내용2", PaymentMethod.BANK_TRANSFER, 20000L, PaymentStatus.READY, now}
      );

      given(paymentRepository.findAllPaymentsWithLatestStatus()).willReturn(mockResults);

      PaymentResponseDto.PaymentList result = paymentService.getAllPayment();

      assertThat(result.totalCount()).isEqualTo(2);
      assertThat(result.payments()).hasSize(2);
      assertThat(result.payments().get(0).paymentId()).isEqualTo(paymentId1);
      assertThat(result.payments().get(1).paymentId()).isEqualTo(paymentId2);
    }

    @Test
    @DisplayName("정상: 특정 결제 상세를 조회한다")
    void 결제_상세_조회_테스트() {
      UUID paymentId = UUID.randomUUID();
      LocalDateTime now = LocalDateTime.now();

      List<Object[]> mockResults = java.util.Collections.singletonList(
          new Object[]{paymentId, "테스트 결제", "테스트 내용", PaymentMethod.CREDIT_CARD, 10000L, PaymentStatus.DONE, now}
      );

      given(paymentRepository.findPaymentWithLatestStatus(paymentId)).willReturn(mockResults);

      PaymentResponseDto.PaymentDetail result = paymentService.getDetailPayment(paymentId);

      assertThat(result.paymentId()).isEqualTo(paymentId);
      assertThat(result.title()).isEqualTo("테스트 결제");
      assertThat(result.totalAmount()).isEqualTo(10000L);
      assertThat(result.status()).isEqualTo("DONE");
    }

    @Test
    @DisplayName("예외: 존재하지 않는 결제 상세 조회시 예외가 발생한다")
    void 존재하지_않는_결제_상세_조회_예외_테스트() {
      UUID paymentId = UUID.randomUUID();

      given(paymentRepository.findPaymentWithLatestStatus(paymentId)).willReturn(List.of());

      assertThatThrownBy(() -> paymentService.getDetailPayment(paymentId))
          .isInstanceOf(com.example.Spot.global.presentation.advice.ResourceNotFoundException.class)
          .hasMessage("결제를 찾을 수 없습니다.");
    }
  }

  @Nested
  @DisplayName("결제 취소 조회 테스트")
  class PaymentCancelQueryTest {

    @Test
    @DisplayName("정상: 전체 취소 목록을 조회한다")
    void 전체_취소_목록_조회_테스트() {
      UUID cancelId1 = UUID.randomUUID();
      UUID cancelId2 = UUID.randomUUID();
      UUID paymentId1 = UUID.randomUUID();
      UUID paymentId2 = UUID.randomUUID();
      LocalDateTime now = LocalDateTime.now();

      List<Object[]> mockResults = java.util.Arrays.asList(
          new Object[]{cancelId1, paymentId1, 10000L, now},
          new Object[]{cancelId2, paymentId2, 20000L, now}
      );

      given(paymentHistoryRepository.findAllByStatusWithPayment(PaymentStatus.CANCELLED))
          .willReturn(mockResults);

      PaymentResponseDto.CancelList result = paymentService.getAllPaymentCancel();

      assertThat(result.totalCount()).isEqualTo(2);
      assertThat(result.cancellations()).hasSize(2);
    }

    @Test
    @DisplayName("정상: 특정 결제의 취소 내역을 조회한다")
    void 특정_결제_취소_내역_조회_테스트() {
      UUID paymentId = UUID.randomUUID();
      UUID cancelId = UUID.randomUUID();
      LocalDateTime now = LocalDateTime.now();

      List<Object[]> mockResults = java.util.Collections.singletonList(
          new Object[]{cancelId, paymentId, 10000L, now}
      );

      given(paymentHistoryRepository.findByPaymentIdAndStatusesWithPayment(eq(paymentId), any()))
          .willReturn(mockResults);

      PaymentResponseDto.CancelList result = paymentService.getDetailPaymentCancel(paymentId);

      assertThat(result.totalCount()).isEqualTo(1);
      assertThat(result.cancellations().get(0).paymentId()).isEqualTo(paymentId);
    }
  }

  @Nested
  @DisplayName("executePaymentBilling 테스트 - 결제 실행")
  class ExecutePaymentBillingTest {

    @Test
    @DisplayName("정상: 결제가 성공적으로 실행된다")
    void 결제_실행_성공_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      PaymentHistoryEntity readyHistory = createMockHistory(paymentId, PaymentStatus.READY);
      PaymentHistoryEntity inProgressHistory = createMockHistory(paymentId, PaymentStatus.IN_PROGRESS);
      PaymentHistoryEntity doneHistory = createMockHistory(paymentId, PaymentStatus.DONE);

      TossPaymentResponse tossResponse = new TossPaymentResponse();
      ReflectionTestUtils.setField(tossResponse, "paymentKey", "toss-payment-key-123");
      ReflectionTestUtils.setField(tossResponse, "amount", 10000L);

      PaymentKeyEntity paymentKeyEntity = PaymentKeyEntity.builder()
          .paymentId(paymentId)
          .paymentKey("toss-payment-key-123")
          .confirmedAt(LocalDateTime.now())
          .build();

      given(paymentHistoryRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId))
          .willReturn(Optional.of(readyHistory));
      given(paymentHistoryRepository.save(any(PaymentHistoryEntity.class)))
          .willReturn(inProgressHistory)
          .willReturn(doneHistory);
      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(tossPaymentClient.requestBillingPayment(anyString(), anyLong(), any(UUID.class), anyString(), anyString(), anyInt()))
          .willReturn(tossResponse);
      given(paymentKeyRepository.save(any(PaymentKeyEntity.class))).willReturn(paymentKeyEntity);

      ReflectionTestUtils.setField(paymentService, "billingKey", "test-billing-key");
      ReflectionTestUtils.setField(paymentService, "customerKey", "test-customer-key");
      ReflectionTestUtils.setField(paymentService, "timeout", 30000);

      PaymentResponseDto.Confirm result = paymentService.executePaymentBilling(paymentId);

      assertThat(result.paymentId()).isEqualTo(paymentId);
      assertThat(result.amount()).isEqualTo(10000L);
      verify(tossPaymentClient, times(1)).requestBillingPayment(anyString(), anyLong(), any(UUID.class), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("예외: 결제 이력이 없으면 예외가 발생한다")
    void 결제_이력_없음_예외_테스트() {
      UUID paymentId = UUID.randomUUID();

      given(paymentHistoryRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> paymentService.executePaymentBilling(paymentId))
          .isInstanceOf(com.example.Spot.global.presentation.advice.ResourceNotFoundException.class)
          .hasMessage("결제 이력을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예외: IN_PROGRESS 상태면 이미 처리된 결제 예외가 발생한다")
    void 이미_진행중인_결제_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentHistoryEntity inProgressHistory = createMockHistory(paymentId, PaymentStatus.IN_PROGRESS);

      given(paymentHistoryRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId))
          .willReturn(Optional.of(inProgressHistory));

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
    StoreEntity store = StoreEntity.builder()
        .name("테스트 가게")
        .roadAddress("서울시 강남구")
        .addressDetail("123-45")
        .build();
    ReflectionTestUtils.setField(store, "id", UUID.randomUUID());

    OrderEntity order = OrderEntity.builder()
        .store(store)
        .userId(userId)
        .orderNumber("ORD-" + orderId.toString().substring(0, 8))
        .pickupTime(java.time.LocalDateTime.now().plusHours(1))
        .build();
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
