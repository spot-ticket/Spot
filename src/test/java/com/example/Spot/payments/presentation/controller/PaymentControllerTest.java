package com.example.Spot.payments.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.payments.application.service.PaymentService;
import com.example.Spot.payments.domain.entity.PaymentEntity.PaymentMethod;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PaymentController.class)
@Import(TestSecurityConfig.class)
class PaymentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PaymentService paymentService;

  private UUID orderId;
  private UUID paymentId;
  private Integer userId;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    paymentId = UUID.randomUUID();
    userId = 1;
  }

  private CustomUserDetails createUserDetails(Role role) {
    UserEntity user = UserEntity.forAuthentication(userId, role);
    return new CustomUserDetails(user);
  }

  private void setSecurityContext(Role role) {
    CustomUserDetails userDetails = createUserDetails(role);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Nested
  @DisplayName("POST /api/payments/{order_id}/confirm - 결제 승인")
  class ConfirmPaymentTest {

    @Test
    @DisplayName("정상: CUSTOMER가 결제를 승인한다")
    void CUSTOMER_결제_승인_성공() throws Exception {
      setSecurityContext(Role.CUSTOMER);

      PaymentRequestDto.Confirm request = PaymentRequestDto.Confirm.builder()
          .title("테스트 결제")
          .content("테스트 내용")
          .userId(userId)
          .orderId(orderId)
          .paymentMethod(PaymentMethod.CREDIT_CARD)
          .paymentAmount(10000L)
          .build();

      PaymentResponseDto.Confirm response = PaymentResponseDto.Confirm.builder()
          .paymentId(paymentId)
          .status("DONE")
          .amount(10000L)
          .approvedAt(LocalDateTime.now())
          .build();

      willDoNothing().given(paymentService).validateOrderOwnership(orderId, userId);
      given(paymentService.preparePayment(any(PaymentRequestDto.Confirm.class))).willReturn(paymentId);
      given(paymentService.executePaymentBilling(paymentId)).willReturn(response);

      mockMvc.perform(post("/api/payments/{order_id}/confirm", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.paymentId").value(paymentId.toString()))
          .andExpect(jsonPath("$.result.status").value("DONE"))
          .andExpect(jsonPath("$.result.amount").value(10000));

      verify(paymentService, times(1)).validateOrderOwnership(orderId, userId);
      verify(paymentService, times(1)).preparePayment(any(PaymentRequestDto.Confirm.class));
      verify(paymentService, times(1)).executePaymentBilling(paymentId);
    }

    @Test
    @DisplayName("정상: OWNER가 결제를 승인한다")
    void OWNER_결제_승인_성공() throws Exception {
      setSecurityContext(Role.OWNER);

      PaymentRequestDto.Confirm request = PaymentRequestDto.Confirm.builder()
          .title("테스트 결제")
          .content("테스트 내용")
          .userId(userId)
          .orderId(orderId)
          .paymentMethod(PaymentMethod.CREDIT_CARD)
          .paymentAmount(10000L)
          .build();

      PaymentResponseDto.Confirm response = PaymentResponseDto.Confirm.builder()
          .paymentId(paymentId)
          .status("DONE")
          .amount(10000L)
          .build();

      willDoNothing().given(paymentService).validateOrderStoreOwnership(orderId, userId);
      given(paymentService.preparePayment(any(PaymentRequestDto.Confirm.class))).willReturn(paymentId);
      given(paymentService.executePaymentBilling(paymentId)).willReturn(response);

      mockMvc.perform(post("/api/payments/{order_id}/confirm", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true));

      verify(paymentService, times(1)).validateOrderStoreOwnership(orderId, userId);
    }

    @Test
    @DisplayName("정상: MANAGER가 결제를 승인한다 (소유권 검증 없음)")
    void MANAGER_결제_승인_성공() throws Exception {
      setSecurityContext(Role.MANAGER);

      PaymentRequestDto.Confirm request = PaymentRequestDto.Confirm.builder()
          .title("테스트 결제")
          .content("테스트 내용")
          .userId(userId)
          .orderId(orderId)
          .paymentMethod(PaymentMethod.CREDIT_CARD)
          .paymentAmount(10000L)
          .build();

      PaymentResponseDto.Confirm response = PaymentResponseDto.Confirm.builder()
          .paymentId(paymentId)
          .status("DONE")
          .amount(10000L)
          .build();

      given(paymentService.preparePayment(any(PaymentRequestDto.Confirm.class))).willReturn(paymentId);
      given(paymentService.executePaymentBilling(paymentId)).willReturn(response);

      mockMvc.perform(post("/api/payments/{order_id}/confirm", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true));

      verify(paymentService, never()).validateOrderOwnership(any(), any());
      verify(paymentService, never()).validateOrderStoreOwnership(any(), any());
    }

    @Test
    @DisplayName("예외: 다른 사용자의 주문에 결제를 시도하면 실패한다")
    void 소유권_없음_결제_승인_실패() throws Exception {
      setSecurityContext(Role.CUSTOMER);

      PaymentRequestDto.Confirm request = PaymentRequestDto.Confirm.builder()
          .title("테스트 결제")
          .content("테스트 내용")
          .userId(userId)
          .orderId(orderId)
          .paymentMethod(PaymentMethod.CREDIT_CARD)
          .paymentAmount(10000L)
          .build();

      willThrow(new IllegalStateException("해당 주문에 대한 접근 권한이 없습니다."))
          .given(paymentService).validateOrderOwnership(orderId, userId);

      mockMvc.perform(post("/api/payments/{order_id}/confirm", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().is5xxServerError());

      verify(paymentService, never()).preparePayment(any());
    }

    @Test
    @DisplayName("정상: 결제 금액이 포함된 결제 승인 요청")
    void 결제_금액_포함_승인_요청() throws Exception {
      setSecurityContext(Role.CUSTOMER);

      PaymentRequestDto.Confirm request = PaymentRequestDto.Confirm.builder()
          .title("고가 결제")
          .content("프리미엄 상품")
          .userId(userId)
          .orderId(orderId)
          .paymentMethod(PaymentMethod.BANK_TRANSFER)
          .paymentAmount(1000000L)
          .build();

      PaymentResponseDto.Confirm response = PaymentResponseDto.Confirm.builder()
          .paymentId(paymentId)
          .status("DONE")
          .amount(1000000L)
          .build();

      willDoNothing().given(paymentService).validateOrderOwnership(orderId, userId);
      given(paymentService.preparePayment(any(PaymentRequestDto.Confirm.class))).willReturn(paymentId);
      given(paymentService.executePaymentBilling(paymentId)).willReturn(response);

      mockMvc.perform(post("/api/payments/{order_id}/confirm", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result.amount").value(1000000));
    }
  }

  @Nested
  @DisplayName("POST /api/payments/{order_id}/cancel - 결제 취소")
  class CancelPaymentTest {

    @Test
    @DisplayName("정상: CUSTOMER가 결제를 취소한다")
    void CUSTOMER_결제_취소_성공() throws Exception {
      setSecurityContext(Role.CUSTOMER);

      PaymentRequestDto.Cancel request = PaymentRequestDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelReason("고객 요청")
          .build();

      PaymentResponseDto.Cancel response = PaymentResponseDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelAmount(10000L)
          .cancelReason("고객 요청")
          .canceledAt(LocalDateTime.now())
          .build();

      willDoNothing().given(paymentService).validateOrderOwnership(orderId, userId);
      given(paymentService.executeCancel(any(PaymentRequestDto.Cancel.class))).willReturn(response);

      mockMvc.perform(post("/api/payments/{order_id}/cancel", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.paymentId").value(paymentId.toString()))
          .andExpect(jsonPath("$.result.cancelReason").value("고객 요청"));

      verify(paymentService, times(1)).executeCancel(any(PaymentRequestDto.Cancel.class));
    }

    @Test
    @DisplayName("정상: MASTER가 결제를 취소한다 (소유권 검증 없음)")
    void MASTER_결제_취소_성공() throws Exception {
      setSecurityContext(Role.MASTER);

      PaymentRequestDto.Cancel request = PaymentRequestDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelReason("관리자 취소")
          .build();

      PaymentResponseDto.Cancel response = PaymentResponseDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelAmount(10000L)
          .cancelReason("관리자 취소")
          .canceledAt(LocalDateTime.now())
          .build();

      given(paymentService.executeCancel(any(PaymentRequestDto.Cancel.class))).willReturn(response);

      mockMvc.perform(post("/api/payments/{order_id}/cancel", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true));

      verify(paymentService, never()).validateOrderOwnership(any(), any());
    }

    @Test
    @DisplayName("정상: OWNER가 가게 주문의 결제를 취소한다")
    void OWNER_결제_취소_성공() throws Exception {
      setSecurityContext(Role.OWNER);

      PaymentRequestDto.Cancel request = PaymentRequestDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelReason("가게 사정")
          .build();

      PaymentResponseDto.Cancel response = PaymentResponseDto.Cancel.builder()
          .paymentId(paymentId)
          .cancelAmount(15000L)
          .cancelReason("가게 사정")
          .canceledAt(LocalDateTime.now())
          .build();

      willDoNothing().given(paymentService).validateOrderStoreOwnership(orderId, userId);
      given(paymentService.executeCancel(any(PaymentRequestDto.Cancel.class))).willReturn(response);

      mockMvc.perform(post("/api/payments/{order_id}/cancel", orderId)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result.cancelAmount").value(15000));

      verify(paymentService, times(1)).validateOrderStoreOwnership(orderId, userId);
    }
  }

  @Nested
  @DisplayName("GET /api/payments - 전체 결제 목록 조회")
  class GetAllPaymentTest {

    @Test
    @DisplayName("정상: MANAGER가 전체 결제 목록을 조회한다")
    void MANAGER_전체_결제_목록_조회_성공() throws Exception {
      setSecurityContext(Role.MANAGER);

      List<PaymentResponseDto.PaymentDetail> payments = java.util.Arrays.asList(
          PaymentResponseDto.PaymentDetail.builder()
              .paymentId(UUID.randomUUID())
              .title("결제1")
              .content("내용1")
              .paymentMethod(PaymentMethod.CREDIT_CARD)
              .totalAmount(10000L)
              .status("DONE")
              .createdAt(LocalDateTime.now())
              .build(),
          PaymentResponseDto.PaymentDetail.builder()
              .paymentId(UUID.randomUUID())
              .title("결제2")
              .content("내용2")
              .paymentMethod(PaymentMethod.BANK_TRANSFER)
              .totalAmount(20000L)
              .status("READY")
              .createdAt(LocalDateTime.now())
              .build()
      );

      PaymentResponseDto.PaymentList response = PaymentResponseDto.PaymentList.builder()
          .payments(payments)
          .totalCount(2)
          .build();

      given(paymentService.getAllPayment()).willReturn(response);

      mockMvc.perform(get("/api/payments"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.totalCount").value(2))
          .andExpect(jsonPath("$.result.payments").isArray())
          .andExpect(jsonPath("$.result.payments.length()").value(2));

      verify(paymentService, times(1)).getAllPayment();
    }

    @Test
    @DisplayName("정상: MASTER가 전체 결제 목록을 조회한다")
    void MASTER_전체_결제_목록_조회_성공() throws Exception {
      setSecurityContext(Role.MASTER);

      PaymentResponseDto.PaymentList response = PaymentResponseDto.PaymentList.builder()
          .payments(java.util.Collections.emptyList())
          .totalCount(0)
          .build();

      given(paymentService.getAllPayment()).willReturn(response);

      mockMvc.perform(get("/api/payments"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.totalCount").value(0));
    }

    @Test
    @DisplayName("정상: 결제 목록이 비어있을 때")
    void 빈_결제_목록_조회() throws Exception {
      setSecurityContext(Role.MANAGER);

      PaymentResponseDto.PaymentList response = PaymentResponseDto.PaymentList.builder()
          .payments(java.util.Collections.emptyList())
          .totalCount(0)
          .build();

      given(paymentService.getAllPayment()).willReturn(response);

      mockMvc.perform(get("/api/payments"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result.payments").isArray())
          .andExpect(jsonPath("$.result.payments").isEmpty());
    }
  }

  @Nested
  @DisplayName("GET /api/payments/{paymentId} - 결제 상세 조회")
  class GetDetailPaymentTest {

    @Test
    @DisplayName("정상: CUSTOMER가 본인 결제 상세를 조회한다")
    void CUSTOMER_결제_상세_조회_성공() throws Exception {
      setSecurityContext(Role.CUSTOMER);

      PaymentResponseDto.PaymentDetail response = PaymentResponseDto.PaymentDetail.builder()
          .paymentId(paymentId)
          .title("테스트 결제")
          .content("테스트 내용")
          .paymentMethod(PaymentMethod.CREDIT_CARD)
          .totalAmount(10000L)
          .status("DONE")
          .createdAt(LocalDateTime.now())
          .build();

      willDoNothing().given(paymentService).validatePaymentOwnership(paymentId, userId);
      given(paymentService.getDetailPayment(paymentId)).willReturn(response);

      mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.paymentId").value(paymentId.toString()))
          .andExpect(jsonPath("$.result.title").value("테스트 결제"))
          .andExpect(jsonPath("$.result.totalAmount").value(10000));

      verify(paymentService, times(1)).validatePaymentOwnership(paymentId, userId);
    }

    @Test
    @DisplayName("정상: OWNER가 가게 결제 상세를 조회한다")
    void OWNER_결제_상세_조회_성공() throws Exception {
      setSecurityContext(Role.OWNER);

      PaymentResponseDto.PaymentDetail response = PaymentResponseDto.PaymentDetail.builder()
          .paymentId(paymentId)
          .title("가게 결제")
          .content("가게 주문 내용")
          .paymentMethod(PaymentMethod.CREDIT_CARD)
          .totalAmount(25000L)
          .status("DONE")
          .createdAt(LocalDateTime.now())
          .build();

      willDoNothing().given(paymentService).validatePaymentStoreOwnership(paymentId, userId);
      given(paymentService.getDetailPayment(paymentId)).willReturn(response);

      mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.totalAmount").value(25000));

      verify(paymentService, times(1)).validatePaymentStoreOwnership(paymentId, userId);
    }

    @Test
    @DisplayName("예외: 다른 사용자의 결제 상세 조회시 실패한다")
    void 타인_결제_상세_조회_실패() throws Exception {
      setSecurityContext(Role.CUSTOMER);

      willThrow(new IllegalStateException("해당 결제에 대한 접근 권한이 없습니다."))
          .given(paymentService).validatePaymentOwnership(paymentId, userId);

      mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
          .andDo(print())
          .andExpect(status().is5xxServerError());

      verify(paymentService, never()).getDetailPayment(any());
    }

    @Test
    @DisplayName("정상: MANAGER가 모든 결제 상세를 조회한다")
    void MANAGER_결제_상세_조회_성공() throws Exception {
      setSecurityContext(Role.MANAGER);

      PaymentResponseDto.PaymentDetail response = PaymentResponseDto.PaymentDetail.builder()
          .paymentId(paymentId)
          .title("관리자 조회")
          .content("관리자가 조회하는 결제")
          .paymentMethod(PaymentMethod.CREDIT_CARD)
          .totalAmount(50000L)
          .status("DONE")
          .createdAt(LocalDateTime.now())
          .build();

      given(paymentService.getDetailPayment(paymentId)).willReturn(response);

      mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true));

      verify(paymentService, never()).validatePaymentOwnership(any(), any());
      verify(paymentService, never()).validatePaymentStoreOwnership(any(), any());
    }
  }

  @Nested
  @DisplayName("GET /api/payments/cancel - 전체 취소 목록 조회")
  class GetAllPaymentCancelTest {

    @Test
    @DisplayName("정상: MANAGER가 전체 취소 목록을 조회한다")
    void MANAGER_전체_취소_목록_조회_성공() throws Exception {
      setSecurityContext(Role.MANAGER);

      List<PaymentResponseDto.CancelDetail> cancellations = java.util.Collections.singletonList(
          PaymentResponseDto.CancelDetail.builder()
              .cancelId(UUID.randomUUID())
              .paymentId(UUID.randomUUID())
              .cancelAmount(10000L)
              .cancelReason("고객 요청")
              .canceledAt(LocalDateTime.now())
              .build()
      );

      PaymentResponseDto.CancelList response = PaymentResponseDto.CancelList.builder()
          .cancellations(cancellations)
          .totalCount(1)
          .build();

      given(paymentService.getAllPaymentCancel()).willReturn(response);

      mockMvc.perform(get("/api/payments/cancel"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.totalCount").value(1));

      verify(paymentService, times(1)).getAllPaymentCancel();
    }

    @Test
    @DisplayName("정상: MASTER가 전체 취소 목록을 조회한다")
    void MASTER_전체_취소_목록_조회_성공() throws Exception {
      setSecurityContext(Role.MASTER);

      List<PaymentResponseDto.CancelDetail> cancellations = java.util.Arrays.asList(
          PaymentResponseDto.CancelDetail.builder()
              .cancelId(UUID.randomUUID())
              .paymentId(UUID.randomUUID())
              .cancelAmount(20000L)
              .cancelReason("관리자 취소")
              .canceledAt(LocalDateTime.now())
              .build(),
          PaymentResponseDto.CancelDetail.builder()
              .cancelId(UUID.randomUUID())
              .paymentId(UUID.randomUUID())
              .cancelAmount(15000L)
              .cancelReason("고객 요청")
              .canceledAt(LocalDateTime.now())
              .build()
      );

      PaymentResponseDto.CancelList response = PaymentResponseDto.CancelList.builder()
          .cancellations(cancellations)
          .totalCount(2)
          .build();

      given(paymentService.getAllPaymentCancel()).willReturn(response);

      mockMvc.perform(get("/api/payments/cancel"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result.totalCount").value(2))
          .andExpect(jsonPath("$.result.cancellations.length()").value(2));
    }
  }

  @Nested
  @DisplayName("GET /api/payments/{paymentId}/cancel - 특정 결제 취소 내역 조회")
  class GetDetailPaymentCancelTest {

    @Test
    @DisplayName("정상: CUSTOMER가 본인 결제의 취소 내역을 조회한다")
    void CUSTOMER_결제_취소_내역_조회_성공() throws Exception {
      setSecurityContext(Role.CUSTOMER);

      List<PaymentResponseDto.CancelDetail> cancellations = java.util.Collections.singletonList(
          PaymentResponseDto.CancelDetail.builder()
              .cancelId(UUID.randomUUID())
              .paymentId(paymentId)
              .cancelAmount(10000L)
              .cancelReason("고객 요청")
              .canceledAt(LocalDateTime.now())
              .build()
      );

      PaymentResponseDto.CancelList response = PaymentResponseDto.CancelList.builder()
          .cancellations(cancellations)
          .totalCount(1)
          .build();

      willDoNothing().given(paymentService).validatePaymentOwnership(paymentId, userId);
      given(paymentService.getDetailPaymentCancel(paymentId)).willReturn(response);

      mockMvc.perform(get("/api/payments/{paymentId}/cancel", paymentId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true))
          .andExpect(jsonPath("$.result.totalCount").value(1))
          .andExpect(jsonPath("$.result.cancellations[0].paymentId").value(paymentId.toString()));

      verify(paymentService, times(1)).validatePaymentOwnership(paymentId, userId);
      verify(paymentService, times(1)).getDetailPaymentCancel(paymentId);
    }

    @Test
    @DisplayName("정상: MASTER가 모든 결제의 취소 내역을 조회한다")
    void MASTER_결제_취소_내역_조회_성공() throws Exception {
      setSecurityContext(Role.MASTER);

      PaymentResponseDto.CancelList response = PaymentResponseDto.CancelList.builder()
          .cancellations(java.util.Collections.emptyList())
          .totalCount(0)
          .build();

      given(paymentService.getDetailPaymentCancel(paymentId)).willReturn(response);

      mockMvc.perform(get("/api/payments/{paymentId}/cancel", paymentId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isSuccess").value(true));

      verify(paymentService, never()).validatePaymentOwnership(any(), any());
      verify(paymentService, times(1)).getDetailPaymentCancel(paymentId);
    }

    @Test
    @DisplayName("정상: OWNER가 가게 결제의 취소 내역을 조회한다")
    void OWNER_결제_취소_내역_조회_성공() throws Exception {
      setSecurityContext(Role.OWNER);

      List<PaymentResponseDto.CancelDetail> cancellations = java.util.Collections.singletonList(
          PaymentResponseDto.CancelDetail.builder()
              .cancelId(UUID.randomUUID())
              .paymentId(paymentId)
              .cancelAmount(30000L)
              .cancelReason("재고 부족")
              .canceledAt(LocalDateTime.now())
              .build()
      );

      PaymentResponseDto.CancelList response = PaymentResponseDto.CancelList.builder()
          .cancellations(cancellations)
          .totalCount(1)
          .build();

      willDoNothing().given(paymentService).validatePaymentStoreOwnership(paymentId, userId);
      given(paymentService.getDetailPaymentCancel(paymentId)).willReturn(response);

      mockMvc.perform(get("/api/payments/{paymentId}/cancel", paymentId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result.cancellations[0].cancelReason").value("재고 부족"));

      verify(paymentService, times(1)).validatePaymentStoreOwnership(paymentId, userId);
    }
  }
}
