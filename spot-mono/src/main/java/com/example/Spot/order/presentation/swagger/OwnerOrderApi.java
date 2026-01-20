package com.example.Spot.order.presentation.swagger;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.order.domain.enums.OrderStatus;
import com.example.Spot.order.presentation.dto.request.OrderAcceptRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderCancelRequestDto;
import com.example.Spot.order.presentation.dto.request.OrderRejectRequestDto;
import com.example.Spot.order.presentation.dto.response.OrderResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "주문 (점주)", description = "점주용 주문 관리 API")
public interface OwnerOrderApi {

    @Operation(summary = "내 매장 주문 목록 조회", description = "점주 매장의 주문 목록을 페이지네이션으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getMyStoreOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "고객 ID (필터)") @RequestParam(required = false) Integer customerId,
            @Parameter(description = "날짜 (필터)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "주문 상태 (필터)") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") Sort.Direction direction);

    @Operation(summary = "내 매장 진행 중인 주문 조회", description = "점주 매장의 진행 중인 주문 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyStoreActiveOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "주문 수락", description = "주문을 수락하고 예상 준비 시간을 설정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수락 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "수락 불가능한 상태"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<OrderResponseDto>> acceptOrder(
            @Parameter(description = "주문 ID") @PathVariable UUID orderId,
            @Valid @RequestBody OrderAcceptRequestDto requestDto);

    @Operation(summary = "주문 거절", description = "주문을 거절합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "거절 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "거절 불가능한 상태"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<OrderResponseDto>> rejectOrder(
            @Parameter(description = "주문 ID") @PathVariable UUID orderId,
            @Valid @RequestBody OrderRejectRequestDto requestDto);

    @Operation(summary = "주문 완료", description = "주문을 완료 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "완료 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "완료 불가능한 상태"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<OrderResponseDto>> completeOrder(
            @Parameter(description = "주문 ID") @PathVariable UUID orderId);

    @Operation(summary = "주문 취소 (매장)", description = "매장에서 주문을 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "취소 불가능한 상태"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<OrderResponseDto>> storeCancelOrder(
            @Parameter(description = "주문 ID") @PathVariable UUID orderId,
            @Valid @RequestBody OrderCancelRequestDto requestDto);
}
