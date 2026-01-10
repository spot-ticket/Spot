package com.example.Spot.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.domain.repository.OrderRepository;
import com.example.Spot.payments.domain.repository.PaymentCancelRepository;
import com.example.Spot.payments.domain.repository.PaymentHistoryRepository;
import com.example.Spot.payments.domain.repository.PaymentKeyRepository;
import com.example.Spot.payments.domain.repository.PaymentRepository;
import com.example.Spot.payments.infrastructure.client.TossPaymentClient;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.store.domain.repository.StoreUserRepository;

public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreUserRepository storeUserRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuOptionRepository menuOptionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentKeyRepository paymentKeyRepository;

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    private PaymentCancelRepository paymentCancelRepository;

    @Mock
    private TossPaymentClient tossPaymentClient;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderServiceImpl(
            orderRepository,
            storeRepository,
            storeUserRepository,
            menuRepository,
            menuOptionRepository,
            paymentRepository,
            paymentKeyRepository,
            paymentHistoryRepository,
            paymentCancelRepository,
            tossPaymentClient
        );
    }

    // ============ Payment Tests ============
    
    @Test
    void 결제를_완료하면_주문상태가_대기중으로_변경된다() {
        StoreEntity store = StoreEntity.builder().name("S").roadAddress("R").build();
        OrderEntity order = OrderEntity.builder().store(store).userId(1).orderNumber("X").request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1)).build();
        UUID id = UUID.randomUUID();

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var resp = service.completePayment(id);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(resp).isNotNull();

        verify(orderRepository).findById(id);
    }

    // ============ Order State Tests ============
    
    @Test
    void 주문을_수락하면_예상시간과_함께_수락상태로_변경된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("ACC-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        order.completePayment();
        
        UUID id = UUID.randomUUID();
        Integer estimatedTime = 20;

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var resp = service.acceptOrder(id, estimatedTime);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(resp.getEstimatedTime()).isEqualTo(estimatedTime);

        verify(orderRepository).findById(id);
    }

    @Test
    void 주문을_거절하면_사유와_함께_거절상태로_변경된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("REJ-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        order.completePayment();
        
        UUID id = UUID.randomUUID();
        String reason = "재료 부족";

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var resp = service.rejectOrder(id, reason);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(resp.getReason()).isEqualTo(reason);

        verify(orderRepository).findById(id);
    }

    @Test
    void 조리를_시작하면_조리중_상태로_변경되고_시작시간이_기록된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("COOK-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        order.completePayment();
        order.acceptOrder(15);

        UUID id = UUID.randomUUID();

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var resp = service.startCooking(id);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.COOKING);
        assertThat(resp.getCookingStartedAt()).isNotNull();

        verify(orderRepository).findById(id);
    }

    @Test
    void 픽업준비를_완료하면_준비완료_상태로_변경되고_완료시간이_기록된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("READY-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        order.completePayment();
        order.acceptOrder(15);
        order.startCooking();

        UUID id = UUID.randomUUID();

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var resp = service.readyForPickup(id);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.READY);
        assertThat(resp.getCookingCompletedAt()).isNotNull();

        verify(orderRepository).findById(id);
    }

    @Test
    void 픽업을_완료하면_완료_상태로_변경되고_픽업시간이_기록된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("COMPLETE-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        order.completePayment();
        order.acceptOrder(15);
        order.startCooking();
        order.readyForPickup();

        UUID id = UUID.randomUUID();

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var resp = service.completeOrder(id);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(resp.getPickedUpAt()).isNotNull();

        verify(orderRepository).findById(id);
    }

    // ============ Order Cancellation Tests ============
    
    @Test
    void 고객이_주문을_취소하면_취소상태로_변경되고_사유와_시간이_기록된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("CANCEL-CUST-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        UUID id = UUID.randomUUID();
        String reason = "마음 바뀜";

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        var resp = service.customerCancelOrder(id, reason);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(resp.getReason()).isEqualTo(reason);
        assertThat(resp.getCancelledAt()).isNotNull();

        verify(orderRepository).findById(id);
    }

    // ============ Order Tests ============
    
    @Test
    void ID로_주문을_조회하면_주문정보가_반환된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("GET-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        UUID id = UUID.randomUUID();

        when(orderRepository.findByIdWithDetails(id)).thenReturn(Optional.of(order));

        var resp = service.getOrderById(id);
        assertThat(resp).isNotNull();
        assertThat(resp.getOrderNumber()).isEqualTo("GET-001");

        verify(orderRepository).findByIdWithDetails(id);
    }

    @Test
    void 주문번호로_주문을_조회하면_주문정보가_반환된다() {
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order = OrderEntity.builder()
                .store(store).userId(1).orderNumber("NUM-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        when(orderRepository.findByOrderNumber("NUM-001")).thenReturn(Optional.of(order));

        var resp = service.getOrderByOrderNumber("NUM-001");
        assertThat(resp).isNotNull();
        assertThat(resp.getOrderNumber()).isEqualTo("NUM-001");

        verify(orderRepository).findByOrderNumber("NUM-001");
    }

    @Test
    void 사용자ID로_주문을_조회하면_모든_주문이_반환된다() {
        Integer userId = 1;
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity order1 = OrderEntity.builder()
                .store(store).userId(userId).orderNumber("USER-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        OrderEntity order2 = OrderEntity.builder()
                .store(store).userId(userId).orderNumber("USER-002")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(2))
                .build();

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order1, order2));

        var resp = service.getUserOrders(userId);
        assertThat(resp).hasSize(2);

        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void 사용자의_활성_주문을_조회하면_진행중인_주문만_반환된다() {
        Integer userId = 1;
        StoreEntity store = StoreEntity.builder().name("Store").roadAddress("Addr").build();
        OrderEntity activeOrder = OrderEntity.builder()
                .store(store).userId(userId).orderNumber("ACTIVE-001")
                .request(null).needDisposables(false).pickupTime(LocalDateTime.now().plusHours(1))
                .build();
        activeOrder.completePayment();

        when(orderRepository.findActiveOrdersByUserId(userId)).thenReturn(List.of(activeOrder));

        var resp = service.getUserActiveOrders(userId);
        assertThat(resp).hasSize(1);

        verify(orderRepository).findActiveOrdersByUserId(userId);
    }
        
    @Test
    void 존재하지_않는_주문을_수락하면_예외가_발생한다() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptOrder(id, 15))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 주문");
    }
}
