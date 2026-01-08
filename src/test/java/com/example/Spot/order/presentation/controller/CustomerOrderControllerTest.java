package com.example.Spot.order.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.order.application.service.OrderService;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.presentation.dto.request.OrderCancelRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderCreateRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

public class CustomerOrderControllerTest {

    private OrderService orderService;
    private CustomerOrderController controller;

    @BeforeEach
    void setup() {
        orderService = Mockito.mock(OrderService.class);
        controller = new CustomerOrderController(orderService);
    }

    @Test
    void 주문을_생성하면_결제대기_상태로_주문이_생성된다() {
        OrderCreateRequestDto req = OrderCreateRequestDto.builder()
                .storeId(UUID.randomUUID())
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        OrderResponseDto mockResp = OrderResponseDto.builder()
                .orderNumber("X")
                .orderStatus(OrderStatus.PAYMENT_PENDING)
                .build();
        when(orderService.createOrder(req, 11)).thenReturn(mockResp);

        CustomUserDetails user = new CustomUserDetails(UserEntity.forAuthentication(11, Role.CUSTOMER));

        var res = controller.createOrder(req, user);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_CREATED.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(mockResp);
        assertThat(res.getBody().getResult().getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        
        verify(orderService).createOrder(req, 11);
    }

    @Test
    void 존재하지_않는_가게에_주문을_생성하면_예외가_발생한다() {
        OrderCreateRequestDto req = OrderCreateRequestDto.builder()
                .storeId(UUID.randomUUID())
                .pickupTime(LocalDateTime.now().plusHours(1))
                .build();

        when(orderService.createOrder(req, 11)).thenThrow(new IllegalArgumentException("존재하지 않는 가게입니다."));

        CustomUserDetails user = new CustomUserDetails(UserEntity.forAuthentication(11, Role.CUSTOMER));

        try {
            controller.createOrder(req, user);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("존재하지 않는 가게");
        }
    }

    @Test
    void 내_주문을_조회하면_주문_목록이_반환된다() {
        CustomUserDetails user = new CustomUserDetails(UserEntity.forAuthentication(5, Role.CUSTOMER));
        OrderResponseDto dto = OrderResponseDto.builder().orderNumber("N").build();
        Page<OrderResponseDto> page = new PageImpl<>(List.of(dto));
        when(orderService.getUserOrdersWithPagination(org.mockito.ArgumentMatchers.eq(5), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(page);

        var res = controller.getMyOrders(user, null, null, null, 0, 20, "createdAt", Sort.Direction.DESC);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_LIST_FOUND.getStatus());
        assertThat(res.getBody().getResult()).hasSize(1);

        verify(orderService).getUserOrdersWithPagination(org.mockito.ArgumentMatchers.eq(5), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 고객이_주문을_취소하면_취소_상태로_변경된다() {
        UUID id = UUID.randomUUID();
        OrderCancelRequestDto req = new OrderCancelRequestDto("not ok");
        OrderResponseDto dto = OrderResponseDto.builder()
                .orderNumber("C")
                .orderStatus(OrderStatus.CANCELLED)
                .build();
        when(orderService.customerCancelOrder(id, "not ok")).thenReturn(dto);

        var res = controller.customerCancelOrder(id, req);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_CANCELLED.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(dto);
        assertThat(res.getBody().getResult().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        
        verify(orderService).customerCancelOrder(id, "not ok");
    }

    @Test
    void 존재하지_않는_주문을_취소하면_예외가_발생한다() {
        UUID id = UUID.randomUUID();
        OrderCancelRequestDto req = new OrderCancelRequestDto("not ok");
        when(orderService.customerCancelOrder(id, "not ok")).thenThrow(new IllegalArgumentException("존재하지 않는 주문입니다."));

        try {
            controller.customerCancelOrder(id, req);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("존재하지 않는 주문");
        }
    }

    @Test
    void 내_활성_주문을_조회하면_진행중인_주문만_반환된다() {
        CustomUserDetails user = new CustomUserDetails(UserEntity.forAuthentication(5, Role.CUSTOMER));
        OrderResponseDto activeDto = OrderResponseDto.builder()
                .orderNumber("ACTIVE")
                .orderStatus(OrderStatus.PENDING)
                .build();
        when(orderService.getUserActiveOrders(5)).thenReturn(List.of(activeDto));

        var res = controller.getMyActiveOrders(user);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_LIST_FOUND.getStatus());
        assertThat(res.getBody().getResult()).hasSize(1);
        
        verify(orderService).getUserActiveOrders(5);
    }
}
