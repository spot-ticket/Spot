package com.example.Spot.global.infrastructure.client.feign.exception;

public class RemoteServiceUnavailableException extends RuntimeException {
    public RemoteServiceUnavailableException(String methodKey) {
        super("Remote service unavailable: " + methodKey);
    }
}
