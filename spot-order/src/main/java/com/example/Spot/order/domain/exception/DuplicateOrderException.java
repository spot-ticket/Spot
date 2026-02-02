package com.example.Spot.order.domain.exception;

public class DuplicateOrderException extends RuntimeException {

    public DuplicateOrderException() {
        super("동일한 가게에 같은 픽업시간, 같은 메뉴로 이미 주문이 존재합니다.");
    }
}
