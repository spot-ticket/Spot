package com.example.Spot.order.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.Spot.order.presentation.dto.request.OrderAcceptRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderCancelRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderRejectRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

public class OwnerOrderControllerTest {

    private OrderService orderService;
    private OwnerOrderController controller;

    @BeforeEach
    void setup() {
        orderService = Mockito.mock(OrderService.class);
        controller = new OwnerOrderController(orderService);
    }

    @Test
    void 내_매장_주문을_조회하면_주문_목록이_반환된다() {
        CustomUserDetails user = new CustomUserDetails(UserEntity.forAuthentication(7, Role.OWNER));
        OrderResponseDto dto = OrderResponseDto.builder().orderNumber("O1").build();
        Page<OrderResponseDto> page = new PageImpl<>(List.of(dto));
        when(orderService.getMyStoreOrdersWithPagination(org.mockito.ArgumentMatchers.eq(7), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(page);

        var res = controller.getMyStoreOrders(user, null, null, null, 0, 20, "createdAt", Sort.Direction.DESC);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_LIST_FOUND.getStatus());
        assertThat(res.getBody().getResult()).hasSize(1);

        verify(orderService).getMyStoreOrdersWithPagination(org.mockito.ArgumentMatchers.eq(7), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 내_매장_활성_주문을_조회하면_진행중인_주문만_반환된다() {
        CustomUserDetails user = new CustomUserDetails(UserEntity.forAuthentication(8, Role.OWNER));
        OrderResponseDto dto = OrderResponseDto.builder()
                .orderNumber("A1")
                .orderStatus(OrderStatus.ACCEPTED)
                .build();
        when(orderService.getMyStoreActiveOrders(8)).thenReturn(List.of(dto));

        var res = controller.getMyStoreActiveOrders(user);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_LIST_FOUND.getStatus());
        assertThat(res.getBody().getResult()).hasSize(1);
        
        verify(orderService).getMyStoreActiveOrders(8);
    }

    @Test
    void 주문을_수락하면_예상시간과_함께_수락_상태로_변경된다() {
        UUID id = UUID.randomUUID();
        OrderAcceptRequestDto req = OrderAcceptRequestDto.builder().estimatedTime(15).build();
        OrderResponseDto dto = OrderResponseDto.builder()
                .orderNumber("AC")
                .orderStatus(OrderStatus.ACCEPTED)
                .estimatedTime(15)
                .build();
        when(orderService.acceptOrder(id, 15)).thenReturn(dto);

        var res = controller.acceptOrder(id, req);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_ACCEPTED.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(dto);
        assertThat(res.getBody().getResult().getEstimatedTime()).isEqualTo(15);
        
        verify(orderService).acceptOrder(id, 15);
    }

    @Test
    void 주문을_거절하면_사유와_함께_거절_상태로_변경된다() {
        UUID id = UUID.randomUUID();
        OrderRejectRequestDto req = OrderRejectRequestDto.builder().reason("no").build();
        OrderResponseDto dto = OrderResponseDto.builder()
                .orderNumber("RJ")
                .orderStatus(OrderStatus.REJECTED)
                .reason("no")
                .build();
        when(orderService.rejectOrder(id, "no")).thenReturn(dto);

        var res = controller.rejectOrder(id, req);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_REJECTED.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(dto);
        assertThat(res.getBody().getResult().getReason()).isEqualTo("no");
        
        verify(orderService).rejectOrder(id, "no");
    }

    @Test
    void 주문을_완료_처리하면_완료_상태로_변경된다() {
        UUID id = UUID.randomUUID();
        OrderResponseDto dto = OrderResponseDto.builder()
                .orderNumber("CP")
                .orderStatus(OrderStatus.COMPLETED)
                .build();
        when(orderService.completeOrder(id)).thenReturn(dto);

        var res = controller.completeOrder(id);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_COMPLETED.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(dto);
        assertThat(res.getBody().getResult().getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        
        verify(orderService).completeOrder(id);
    }

    @Test
    void 가게가_주문을_취소하면_사유와_함께_취소_상태로_변경된다() {
        UUID id = UUID.randomUUID();
        OrderCancelRequestDto req = OrderCancelRequestDto.builder().reason("store problem").build();
        OrderResponseDto dto = OrderResponseDto.builder()
                .orderNumber("SC")
                .orderStatus(OrderStatus.CANCELLED)
                .reason("store problem")
                .build();
        when(orderService.storeCancelOrder(id, "store problem")).thenReturn(dto);

        var res = controller.storeCancelOrder(id, req);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_CANCELLED.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(dto);
        assertThat(res.getBody().getResult().getReason()).isEqualTo("store problem");
        
        verify(orderService).storeCancelOrder(id, "store problem");
    }

    @Test
    void 존재하지_않는_주문을_가게가_취소하면_예외가_발생한다() {
        UUID id = UUID.randomUUID();
        OrderCancelRequestDto req = OrderCancelRequestDto.builder().reason("problem").build();
        when(orderService.storeCancelOrder(id, "problem")).thenThrow(new IllegalArgumentException("존재하지 않는 주문입니다."));

        try {
            controller.storeCancelOrder(id, req);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("존재하지 않는 주문");
        }
    }
}
