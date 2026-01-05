package com.example.Spot.order.presentation.code;

import org.springframework.http.HttpStatus;

import com.example.Spot.global.presentation.code.BaseSuccessCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderSuccessCode implements BaseSuccessCode {

    ORDER_CREATED(HttpStatus.CREATED,
            "ORDER201_1",
            "주문이 생성되었습니다."),
    ORDER_FOUND(HttpStatus.OK,
            "ORDER200_1",
            "주문을 조회했습니다."),
    ORDER_LIST_FOUND(HttpStatus.OK,
            "ORDER200_2",
            "주문 목록을 조회했습니다."),
    ORDER_ACCEPTED(HttpStatus.OK,
            "ORDER200_3",
            "주문을 수락했습니다."),
    ORDER_REJECTED(HttpStatus.OK,
            "ORDER200_4",
            "주문을 거절했습니다."),
    ORDER_COOKING_STARTED(HttpStatus.OK,
            "ORDER200_5",
            "조리를 시작했습니다."),
    ORDER_READY(HttpStatus.OK,
            "ORDER200_6",
            "픽업 준비가 완료되었습니다."),
    ORDER_COMPLETED(HttpStatus.OK,
            "ORDER200_7",
            "주문이 완료되었습니다."),
    ORDER_CANCELLED(HttpStatus.OK,
            "ORDER200_8",
            "주문이 취소되었습니다."),
    PAYMENT_COMPLETED(HttpStatus.OK,
            "ORDER200_9",
            "결제가 완료되었습니다."),
    PAYMENT_FAILED(HttpStatus.OK,
            "ORDER200_10",
            "결제가 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

