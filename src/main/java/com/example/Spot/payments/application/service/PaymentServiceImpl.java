package com.example.Spot.payments.application.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.presentation.advice.ResourceNotFoundException;
import com.example.Spot.payments.presentation.dto.response.PaymentResponseDto;
import com.example.Spot.payments.presentation.dto.request.PaymentRequestDto;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.domain.repository.PaymentCancelRepository;
import com.example.Spot.payments.domain.repository.PaymentItemRepository;
import com.example.Spot.payments.domain.entity.PaymentEntity;
import com.example.Spot.payments.domain.entity.PaymentItemEntity;
import com.example.Spot.payments.domain.entity.PaymentCancelEntity;
import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TossPaymentClient tossPaymentClient;

    @Value("${toss.payments.billingKey}")
    private String billingKey;

    @Value("${toss.payments.customerKey}")
    private String customerKey;

    private final PaymentRepository paymentRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentCancelRepository paymentCancelRepository;

    // 주문 수락 이후에 동작되어야 함
    // 프론트가 없으니까 자동 결제로 대체. 결제는 프론트 페이지가 요구됨
    // billingKey를 받으려면 프론트 로직이 요구되어서 customer_id는 1로 고정됨
    @Override
    @Transactional
    public PaymentResponseDto.Confirm paymentBillingConfirm(PaymentRequestDto.Confirm request) {
        // READY -> IN_PROGRESS -> DONE / ABORTED
        // https://docs.tosspayments.com/reference/using-api/webhook-events 참고

        // payment item에 기록 저장
        validatePaymentRequest(request);

        OrderEntity order = findOrder(request.orderId());
        UserEntity user = findUser(request.userId());

        PaymentEntity payment = createPayment(user, request);
        paymentRepository.save(payment);

        createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.READY);

        try {
            createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.IN_PROGRESS);

            TossPaymentResponse tossResponse = tossPaymentClient.requestBillingPayment(
                this.billingKey,
                request.paymentAmount(),
                order.getId().toString(),
                request.title(),
                this.customerKey
            );

            payment.confirm(tossResponse.getPaymentKey());
            PaymentItemEntity paymentItem = createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.DONE);

            return PaymentResponseDto.Confirm.builder()
                .paymentItemId(paymentItem.getId())
                .status(paymentItem.getPaymentStatus().toString())
                .amount(payment.getTotalAmount())
                .approvedAt(payment.getCreatedAt())
                .build();

        } catch (Exception e) {
            createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.ABORTED);
            throw new RuntimeException("자동결제 실패: " + e.getMessage(), e);
        }
    }

    // 결제했을 때 발급받은 paymentKey를 이용함
    @Override
    @Transactional
    public PaymentResponseDto.Cancel paymentCancel(PaymentRequestDto.Cancel request) {

        PaymentEntity payment = paymentRepository.findById(request.paymentId())
            .orElseThrow(() -> new ResourceNotFoundException("결제를 찾을 수 없습니다."));

        // PaymentItem에서 최신 해당 payment에 대해서 최신 것의 status를 확인
        PaymentItemEntity latestItem = paymentItemRepository
            .findTopByPaymentOrderByCreatedAtDesc(payment)
            .orElseThrow(() -> new ResourceNotFoundException("결제 항목을 찾을 수 없습니다."));

        if (latestItem.getPaymentStatus() != PaymentItemEntity.PaymentStatus.DONE) {
            throw new IllegalStateException("완료된 결제만 취소할 수 있습니다. 현재 상태: " + latestItem.getPaymentStatus());
        }

        if (payment.getPaymentKey() == null) {
            throw new IllegalStateException("결제 키가 없어 취소할 수 없습니다.");
        }

        try {
            TossPaymentResponse tossResponse = tossPaymentClient.cancelPayment(
                payment.getPaymentKey(),
                request.cancelReason()
            );

            OrderEntity order = latestItem.getOrder();
            PaymentItemEntity cancelledItem = createPaymentItem(payment, order, PaymentItemEntity.PaymentStatus.CANCELLED);

            PaymentCancelEntity cancelEntity = PaymentCancelEntity.builder()
                .paymentItem(cancelledItem)
                .reason(request.cancelReason())
                .cancelIdempotency(UUID.randomUUID())
                .build();
            paymentCancelRepository.save(cancelEntity);

            return PaymentResponseDto.Cancel.builder()
                .paymentId(payment.getId())
                .cancelAmount(payment.getTotalAmount())
                .cancelReason(request.cancelReason())
                .canceledAt(cancelEntity.getCreatedAt())
                .build();

        } catch (Exception e) {
            throw new RuntimeException("결제 취소 실패: " + e.getMessage(), e);
        }
    }

    private void validatePaymentRequest(PaymentRequestDto.Confirm request) {
        if (request.paymentAmount() <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다");
        }
    }

    private OrderEntity findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
    }

    private UserEntity findUser(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private PaymentEntity createPayment(UserEntity user, PaymentRequestDto.Confirm request) {
        return PaymentEntity.builder()
                            .user(user)
                            .title(request.title())
                            .content(request.content())
                            .paymentMethod(request.paymentMethod())
                            .totalAmount(request.paymentAmount())
                            .build();
    }

    private PaymentItemEntity createPaymentItem(PaymentEntity payment, OrderEntity order, PaymentItemEntity.PaymentStatus status) {
        PaymentItemEntity paymentItem = PaymentItemEntity.builder()
                                                         .payment(payment)
                                                         .order(order)
                                                         .status(status)
                                                         .build();

        paymentItemRepository.save(paymentItem);

        return paymentItem;
    }

    @Override
    public PaymentResponseDto.View paymentUserView() {
        // TODO: 구현 필요
        return null;
    }

}
