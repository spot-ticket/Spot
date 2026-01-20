package com.example.Spot.order.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.order.application.service.OrderService;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

public class ChefOrderControllerTest {

    private OrderService orderService;
    private ChefOrderController controller;

    @BeforeEach
    void setup() {
        orderService = Mockito.mock(OrderService.class);
        controller = new ChefOrderController(orderService);
    }

    @Test
    void 셰프가_오늘의_주문을_조회하면_주문_목록이_반환된다() {
        CustomUserDetails user = new CustomUserDetails(UserEntity.forAuthentication(21, Role.CHEF));
        OrderResponseDto dto = OrderResponseDto.builder().orderNumber("Chef1").build();
        when(orderService.getChefTodayOrders(21)).thenReturn(List.of(dto));

        var res = controller.getChefTodayOrders(user);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_LIST_FOUND.getStatus());
        assertThat(res.getBody().getResult()).hasSize(1);
        
        verify(orderService).getChefTodayOrders(21);
    }

    @Test
    void 조리를_시작하면_조리중_상태로_변경된다() {
        UUID id = UUID.randomUUID();
        OrderResponseDto dto = OrderResponseDto.builder().orderNumber("S").build();
        when(orderService.startCooking(id)).thenReturn(dto);

        var res = controller.startCooking(id);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_COOKING_STARTED.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(dto);
        
        verify(orderService).startCooking(id);
    }

    @Test
    void 존재하지_않는_주문의_조리를_시작하면_예외가_발생한다() {
        UUID id = UUID.randomUUID();
        when(orderService.startCooking(id)).thenThrow(new IllegalArgumentException("존재하지 않는 주문입니다."));

        try {
            controller.startCooking(id);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("존재하지 않는 주문");
        }
    }

    @Test
    void 픽업_준비를_완료하면_준비완료_상태로_변경된다() {
        UUID id = UUID.randomUUID();
        OrderResponseDto dto = OrderResponseDto.builder().orderNumber("R").build();
        when(orderService.readyForPickup(id)).thenReturn(dto);

        var res = controller.readyForPickup(id);
        assertThat(res.getStatusCode()).isEqualTo(com.example.Spot.order.presentation.code.OrderSuccessCode.ORDER_READY.getStatus());
        assertThat(res.getBody().getResult()).isEqualTo(dto);
        
        verify(orderService).readyForPickup(id);
    }
}
