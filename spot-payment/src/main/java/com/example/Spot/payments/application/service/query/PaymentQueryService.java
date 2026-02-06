package com.example.Spot.payments.application.service.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.feign.OrderClient;
import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.dto.OrderResponse;
import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentHistoryEntity;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    private final OrderClient orderClient;
    private final StoreClient storeClient;

    public PaymentResponseDto.PaymentList getAllPayments() {
        List<Object[]> results = paymentRepository.findAllPaymentsWithLatestStatus();

        List<PaymentResponseDto.PaymentDetail> payments = results.stream()
                .map(this::mapToPaymentDetail)
                .collect(Collectors.toList());

        return PaymentResponseDto.PaymentList.builder()
                .payments(payments)
                .totalCount(payments.size())
                .build();
    }

    public PaymentResponseDto.PaymentDetail getPaymentDetail(UUID paymentId) {
        List<Object[]> results = paymentRepository.findPaymentWithLatestStatus(paymentId);

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("[PaymentQueryService] 결제를 찾을 수 없습니다.");
        }

        return mapToPaymentDetail(results.get(0));
    }

    public PaymentResponseDto.CancelList getAllCancellations() {
        List<Object[]> results = paymentHistoryRepository.findAllByStatusWithPayment(
                PaymentHistoryEntity.PaymentStatus.CANCELLED);

        List<PaymentResponseDto.CancelDetail> cancellations = results.stream()
                .map(row -> mapToCancelDetail(row, null))
                .collect(Collectors.toList());

        return PaymentResponseDto.CancelList.builder()
                .cancellations(cancellations)
                .totalCount(cancellations.size())
                .build();
    }

    public PaymentResponseDto.CancelList getCancellationsByPaymentId(UUID paymentId) {
        List<PaymentHistoryEntity.PaymentStatus> cancelStatuses = List.of(
                PaymentHistoryEntity.PaymentStatus.CANCELLED,
                PaymentHistoryEntity.PaymentStatus.PARTIAL_CANCELLED);

        List<Object[]> results = paymentHistoryRepository.findByPaymentIdAndStatusesWithPayment(
                paymentId, cancelStatuses);

        List<PaymentResponseDto.CancelDetail> cancellations = results.stream()
                .map(row -> mapToCancelDetail(row, null))
                .collect(Collectors.toList());

        return PaymentResponseDto.CancelList.builder()
                .cancellations(cancellations)
                .totalCount(cancellations.size())
                .build();
    }

    public boolean existsById(UUID paymentId) {
        return paymentRepository.existsById(paymentId);
    }

    public PaymentEntity findPayment(UUID paymentId) {
        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("[PaymentQueryService] 결제를 찾을 수 없습니다."));
    }

    public void validateOrderStoreOwnership(UUID orderId, Integer userId) {
        OrderResponse order = findOrder(orderId);
        UUID storeId = order.getStoreId();

        if (!storeClient.existsByStoreIdAndUserId(storeId, userId)) {
            throw new IllegalStateException("[PaymentQueryService] 해당 주문의 가게에 대한 접근 권한이 없습니다.");
        }
    }

    public void validatePaymentStoreOwnership(UUID paymentId, Integer userId) {
        PaymentEntity payment = findPayment(paymentId);

        OrderResponse order = findOrder(payment.getOrderId());
        UUID storeId = order.getStoreId();

        if (!storeClient.existsByStoreIdAndUserId(storeId, userId)) {
            throw new IllegalStateException("[PaymentQueryService] 해당 결제의 가게에 대한 접근 권한이 없습니다.");
        }
    }

    private OrderResponse findOrder(UUID orderId) {
        OrderResponse order = orderClient.getOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("[PaymentQueryService] 주문을 찾을 수 없습니다.");
        }
        return order;
    }

    private PaymentResponseDto.PaymentDetail mapToPaymentDetail(Object[] row) {
        return PaymentResponseDto.PaymentDetail.builder()
                .paymentId((UUID) row[0])
                .title((String) row[1])
                .content((String) row[2])
                .paymentMethod((PaymentEntity.PaymentMethod) row[3])
                .totalAmount((Long) row[4])
                .status(((PaymentHistoryEntity.PaymentStatus) row[5]).toString())
                .createdAt((LocalDateTime) row[6])
                .build();
    }

    private PaymentResponseDto.CancelDetail mapToCancelDetail(Object[] row, String cancelReason) {
        return PaymentResponseDto.CancelDetail.builder()
                .cancelId((UUID) row[0])
                .paymentId((UUID) row[1])
                .cancelAmount((Long) row[2])
                .cancelReason(cancelReason)
                .canceledAt((LocalDateTime) row[3])
                .build();
    }
}
