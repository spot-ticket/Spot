package com.example.Spot.order.application; 

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.global.feign.PaymentClient;
import com.example.Spot.global.feign.StoreClient;
import com.example.Spot.global.feign.dto.StoreResponse;
import com.example.Spot.order.application.service.OrderServiceImpl;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.domain.exception.DuplicateOrderException;
import com.example.Spot.order.domain.repository.OrderItemOptionRepository;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.order.infrastructure.aop.OrderValidationContext;
import com.example.Spot.order.infrastructure.producer.OrderEventProducer;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock private OrderItemOptionRepository orderItemOptionRepository;
    @Mock private PaymentClient paymentClient;
    @Mock private StoreClient storeClient;
    @Mock private OrderEventProducer orderEventProducer; // 아웃박스 관련은 여기서 Mock!
    
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("주문 생성 및 이벤트 발행 호출 확인")
    void createOrderSuccess() {
        // 1. 가짜 데이터 준비
        UUID storeId = UUID.randomUUID();
        StoreResponse mockStore = StoreResponse.builder()
                .id(storeId)
                .name("가게이름")
                .build();

        // ⭐ 중요: Request DTO에 필수 값들을 채워줍니다.
        OrderCreateRequestDto request = new OrderCreateRequestDto();

        // 만약 필드에 직접 접근이 안된다면 ReflectionTestUtils를 사용하세요.
        ReflectionTestUtils.setField(request, "pickupTime", LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(request, "needDisposables", true);
        ReflectionTestUtils.setField(request, "orderItems", List.of()); // 아이템 리스트도 빈 리스트라도 필요함

        // 2. 가짜 동작(given) 설정
        given(orderRepository.findActiveOrdersByUserAndStoreAndPickupTime(any(), any(), any()))
                .willReturn(List.of());
        given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // 3. Static 컨텍스트 모킹
        try (MockedStatic<OrderValidationContext> mockedContext = mockStatic(OrderValidationContext.class)) {
            mockedContext.when(OrderValidationContext::getStoreResponse).thenReturn(mockStore);

            // when
            orderService.createOrder(request, 1);

            // then
            verify(orderRepository).save(any());
            verify(orderEventProducer).reserveOrderCreated(any(), any(), anyLong());
        }
    }

    @Test
    @DisplayName("결제 완료 시 주문 상태 변경 및 펜딩 이벤트 발행")
    void completePaymentTest() {
        // 1. 빌더에 있는 필드들만 넣어서 객체 생성
        // (orderStatus는 생성자 내부 로직에 의해 자동으로 PAYMENT_PENDING이 됩니다)
        OrderEntity order = OrderEntity.builder()
                .userId(1)
                .storeId(UUID.randomUUID())
                .orderNumber("ORDER-2024-0001")
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        // 2. ID 주입 (로직에서 ID를 쓸 수도 있으므로)
        UUID orderId = UUID.randomUUID();
        ReflectionTestUtils.setField(order, "id", orderId);

        // 3. Spy 설정 및 Repository 동작 정의
        OrderEntity spyOrder = spy(order);
        given(orderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(spyOrder));

        // when
        orderService.completePayment(orderId);

        // then
        // 진짜 서비스 로직인 completePayment()가 호출되면 PENDING으로 바뀌어야 함
        assertThat(spyOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderEventProducer).reserveOrderPending(any(), eq(orderId));
    }

    @Test
    @DisplayName("가게 사장이 주문 거절 시 상태가 CANCEL_PENDING으로 변경되고 취소 이벤트가 발행된다")
    void rejectOrderTest() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderEntity order = spy(OrderEntity.builder()
                .userId(1)
                .storeId(UUID.randomUUID())
                .orderNumber("ORDER-REJECT-001")
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build());

        // rejectOrder는 PENDING 상태에서 가능하므로 상태 강제 주입
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "id", orderId);

        // AOP 컨텍스트가 이 주문을 가지고 있다고 가정
        try (MockedStatic<OrderValidationContext> mockedContext = mockStatic(OrderValidationContext.class)) {
            mockedContext.when(OrderValidationContext::getCurrentOrder).thenReturn(order);

            // when
            orderService.rejectOrder(orderId, "재료 소진");

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCEL_PENDING);
            verify(orderEventProducer).reserveOrderCancelled(eq(orderId), eq("재료 소진"));
        }
    }

    @Test
    @DisplayName("동일한 시간에 동일한 메뉴로 주문하면 DuplicateOrderException이 발생한다")
    void duplicateOrderFailTest() {
        // 1. 가짜 데이터 준비
        UUID storeId = UUID.randomUUID();
        LocalDateTime pickupTime = LocalDateTime.now().plusHours(1);
        StoreResponse mockStore = StoreResponse.builder().id(storeId).build();

        // 이미 존재하는 주문이 있다고 가정 (중복 조건)
        OrderEntity existingOrder = OrderEntity.builder()
                .userId(1).storeId(storeId).orderNumber("OLD-1").pickupTime(pickupTime).build();

        // 2. 가짜 동작(given) 설정
        given(orderRepository.findActiveOrdersByUserAndStoreAndPickupTime(any(), any(), any()))
                .willReturn(List.of(existingOrder));

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        ReflectionTestUtils.setField(request, "pickupTime", pickupTime);
        ReflectionTestUtils.setField(request, "orderItems", List.of()); // 아이템 비교를 위해 빈 리스트 주입

        // 3. ⭐ Static 컨텍스트 모킹 추가
        try (MockedStatic<OrderValidationContext> mockedContext = mockStatic(OrderValidationContext.class)) {
            mockedContext.when(OrderValidationContext::getStoreResponse).thenReturn(mockStore);

            // when & then
            // 이제 store.getId()가 null이 아니므로 중복 체크 로직까지 무사히 진입합니다.
            assertThrows(DuplicateOrderException.class, () -> {
                orderService.createOrder(request, 1);
            });
        }
    }
    
    @Test
    @DisplayName("사장님이 조리를 시작하면 상태가 COOKING으로 변경된다")
    void startCookingTest() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderEntity order = spy(OrderEntity.builder()
                .userId(1).storeId(UUID.randomUUID()).orderNumber("ORD-001")
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build());

        // 조리 시작은 ACCEPTED 상태여야 함
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.ACCEPTED);

        // AOP(OrderStatusChange)가 사용할 Context 모킹
        try (MockedStatic<OrderValidationContext> mockedContext = mockStatic(OrderValidationContext.class)) {
            mockedContext.when(OrderValidationContext::getCurrentOrder).thenReturn(order);

            // when
            orderService.startCooking(orderId);

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COOKING);
            assertThat(order.getCookingStartedAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("조리가 완료되면 상태가 READY로 변경된다")
    void readyForPickupTest() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderEntity order = spy(OrderEntity.builder()
                .userId(1).storeId(UUID.randomUUID()).orderNumber("ORD-002")
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build());

        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.COOKING);

        try (MockedStatic<OrderValidationContext> mockedContext = mockStatic(OrderValidationContext.class)) {
            mockedContext.when(OrderValidationContext::getCurrentOrder).thenReturn(order);

            // when
            orderService.readyForPickup(orderId);

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.READY);
            assertThat(order.getCookingCompletedAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("손님이 음식을 픽업하면 최종 COMPLETED 상태가 된다")
    void completeOrderTest() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderEntity order = spy(OrderEntity.builder()
                .userId(1).storeId(UUID.randomUUID()).orderNumber("ORD-003")
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build());

        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.READY);

        try (MockedStatic<OrderValidationContext> mockedContext = mockStatic(OrderValidationContext.class)) {
            mockedContext.when(OrderValidationContext::getCurrentOrder).thenReturn(order);

            // when
            orderService.completeOrder(orderId);

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(order.getPickedUpAt()).isNotNull();
        }
    }
}
