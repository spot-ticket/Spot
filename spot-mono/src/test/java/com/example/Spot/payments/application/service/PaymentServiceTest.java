package com.example.Spot.payments.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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

import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity.PaymentStatus;
import com.example.Spot.payments.domain.entity.PaymentKeyEntity;
import com.example.Spot.payments.domain.entity.UserBillingAuthEntity;
import com.example.Spot.payments.domain.gateway.PaymentGateway;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentRetryRepository;
import com.example.Spot.payments.domain.repository.UserBillingAuthRepository;
import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.store.domain.repository.StoreUserRepository;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentGateway paymentGateway;
  @Mock private PaymentRepository paymentRepository;
  @Mock private PaymentHistoryRepository paymentHistoryRepository;
  @Mock private PaymentRetryRepository paymentRetryRepository;
  @Mock private PaymentKeyRepository paymentKeyRepository;
  @Mock private UserRepository userRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private StoreUserRepository storeUserRepository;
  @Mock private UserBillingAuthRepository userBillingAuthRepository;

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
  @DisplayName("ready 테스트 - 결제 준비")
  class PaymentReadyTest {

    @Test
    @DisplayName("정상: 새로운 결제 요청이 성공한다")
    void 결제_준비_성공_테스트() {
      UserEntity user = createMockUser(userId);
      OrderEntity order = createMockOrder(orderId);
      PaymentEntity payment = createMockPayment(orderId);

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(paymentRepository.save(any(PaymentEntity.class))).willReturn(payment);

      UUID resultPaymentId = paymentService.ready(request);

      assertThat(resultPaymentId).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("예외: 존재하지 않는 사용자면 예외가 발생한다")
    void 존재하지_않는_사용자_예외_테스트() {
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> paymentService.ready(request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("예외: 존재하지 않는 주문이면 예외가 발생한다")
    void 존재하지_않는_주문_예외_테스트() {
      UserEntity user = createMockUser(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(orderRepository.findById(orderId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> paymentService.ready(request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("주문을 찾을 수 없습니다");
    }
  }

  @Nested
  @DisplayName("createPaymentBillingApprove 테스트 - 결제 승인")
  class PaymentBillingApproveTest {

    @Test
    @DisplayName("정상: 결제가 성공적으로 실행된다")
    void 결제_실행_성공_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      UserBillingAuthEntity billingAuth = UserBillingAuthEntity.builder()
          .userId(userId)
          .authKey("test-auth-key")
          .customerKey("test-customer-key")
          .billingKey("test-billing-key")
          .issuedAt(LocalDateTime.now())
          .build();

      TossPaymentResponse tossResponse = new TossPaymentResponse();
      ReflectionTestUtils.setField(tossResponse, "paymentKey", "toss-payment-key-123");
      ReflectionTestUtils.setField(tossResponse, "totalAmount", 10000L);

      ReflectionTestUtils.setField(paymentService, "timeout", 30000);

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(userBillingAuthRepository.findActiveByUserId(userId)).willReturn(Optional.of(billingAuth));
      given(paymentGateway.requestBillingPayment(anyString(), anyLong(), any(UUID.class), anyString(), anyString(), anyInt()))
          .willReturn(tossResponse);

      PaymentResponseDto.Confirm result = paymentService.createPaymentBillingApprove(paymentId);

      assertThat(result.paymentId()).isEqualTo(paymentId);
      assertThat(result.amount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("예외: 결제를 찾을 수 없으면 예외가 발생한다")
    void 결제_없음_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> paymentService.createPaymentBillingApprove(paymentId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("결제를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("예외: 등록된 결제 수단이 없으면 예외가 발생한다")
    void 결제_수단_없음_예외_테스트() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity payment = createMockPayment(orderId);
      ReflectionTestUtils.setField(payment, "id", paymentId);

      given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
      given(userBillingAuthRepository.findActiveByUserId(userId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> paymentService.createPaymentBillingApprove(paymentId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("등록된 결제 수단이 없습니다");
    }
  }

  @Nested
  @DisplayName("executeCancel 테스트 - 결제 취소")
  class PaymentCancelTest {

    @Test
    @DisplayName("정상: 결제 취소가 성공한다")
    void 결제_취소_성공_테스트() {
      UUID paymentId = UUID.randomUUID();

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

      given(paymentKeyRepository.findByPaymentId(paymentId)).willReturn(Optional.of(paymentKey));
      given(paymentGateway.cancelPayment(anyString(), anyString(), anyInt()))
          .willReturn(tossResponse);

      PaymentResponseDto.Cancel result = paymentService.executeCancel(cancelRequest);

      assertThat(result.paymentId()).isEqualTo(paymentId);
      assertThat(result.cancelReason()).isEqualTo("고객 요청");
    }

    @Test
    @DisplayName("예외: 결제 키가 없으면 취소할 수 없다")
    void 결제_키_없음_취소_예외_테스트() {
      UUID paymentId = UUID.randomUUID();

      PaymentRequestDto.Cancel cancelRequest = PaymentRequestDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelReason("고객 요청")
          .build();

      given(paymentKeyRepository.findByPaymentId(paymentId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> paymentService.executeCancel(cancelRequest))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("결제 키가 없어 취소할 수 없습니다");
    }
  }

  @Nested
  @DisplayName("validateOrderStoreOwnership 테스트 - 가게 소유권 검증")
  class StoreOwnershipValidationTest {

    @Test
    @DisplayName("정상: 가게 소유자가 주문에 접근할 수 있다")
    void 가게_소유자_주문_접근_성공_테스트() {
      OrderEntity order = createMockOrder(orderId);
      UUID storeId = order.getStoreId();

      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(storeUserRepository.existsByStoreIdAndUserId(storeId, userId)).willReturn(true);

      paymentService.validateOrderStoreOwnership(orderId, userId);

      verify(storeUserRepository).existsByStoreIdAndUserId(storeId, userId);
    }

    @Test
    @DisplayName("예외: 가게 소유자가 아니면 주문에 접근할 수 없다")
    void 가게_소유자_아님_주문_접근_예외_테스트() {
      OrderEntity order = createMockOrder(orderId);
      UUID storeId = order.getStoreId();
      Integer otherUserId = 999;

      given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
      given(storeUserRepository.existsByStoreIdAndUserId(storeId, otherUserId)).willReturn(false);

      assertThatThrownBy(() -> paymentService.validateOrderStoreOwnership(orderId, otherUserId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("해당 주문의 가게에 대한 접근 권한이 없습니다");
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
    }

    @Test
    @DisplayName("예외: 존재하지 않는 결제 상세 조회시 예외가 발생한다")
    void 존재하지_않는_결제_상세_조회_예외_테스트() {
      UUID paymentId = UUID.randomUUID();

      given(paymentRepository.findPaymentWithLatestStatus(paymentId)).willReturn(List.of());

      assertThatThrownBy(() -> paymentService.getDetailPayment(paymentId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("결제를 찾을 수 없습니다");
    }
  }

  @Nested
  @DisplayName("saveBillingKey 테스트 - 빌링 인증 정보 저장")
  class SaveBillingKeyTest {

    @Test
    @DisplayName("정상: 빌링 키를 저장한다")
    void 빌링_키_저장_성공_테스트() {
      UserEntity user = createMockUser(userId);

      PaymentRequestDto.SaveBillingKey request = PaymentRequestDto.SaveBillingKey.builder()
          .userId(userId)
          .authKey("test-auth-key")
          .customerKey("test-customer-key")
          .build();

      TossPaymentResponse billingResponse = new TossPaymentResponse();
      ReflectionTestUtils.setField(billingResponse, "billingKey", "issued-billing-key");

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userBillingAuthRepository.findActiveByUserId(userId)).willReturn(Optional.empty());
      given(paymentGateway.issueBillingKey(anyString(), anyString())).willReturn(billingResponse);
      given(userBillingAuthRepository.save(any(UserBillingAuthEntity.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      PaymentResponseDto.SavedBillingKey result = paymentService.saveBillingKey(request);

      assertThat(result.userId()).isEqualTo(userId);
      assertThat(result.customerKey()).isEqualTo("test-customer-key");
    }
  }

  @Nested
  @DisplayName("hasBillingAuth 테스트 - 빌링 인증 정보 존재 여부")
  class HasBillingAuthTest {

    @Test
    @DisplayName("정상: 빌링 인증 정보가 있으면 true를 반환한다")
    void 빌링_인증_존재_테스트() {
      given(userBillingAuthRepository.existsByUserIdAndIsActiveTrue(userId)).willReturn(true);
      given(userBillingAuthRepository.findActiveByUserId(userId)).willReturn(Optional.empty());

      boolean result = paymentService.hasBillingAuth(userId);

      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("정상: 빌링 인증 정보가 없으면 false를 반환한다")
    void 빌링_인증_없음_테스트() {
      given(userBillingAuthRepository.existsByUserIdAndIsActiveTrue(userId)).willReturn(false);
      given(userBillingAuthRepository.findActiveByUserId(userId)).willReturn(Optional.empty());

      boolean result = paymentService.hasBillingAuth(userId);

      assertThat(result).isFalse();
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
    UUID storeId = UUID.randomUUID();

    OrderEntity order = OrderEntity.builder()
        .storeId(storeId)
        .userId(userId)
        .orderNumber("ORD-" + orderId.toString().substring(0, 8))
        .pickupTime(LocalDateTime.now().plusHours(1))
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
}
