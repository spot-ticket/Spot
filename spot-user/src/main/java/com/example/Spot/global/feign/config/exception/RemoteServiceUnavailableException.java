package com.example.Spot.global.feign.config.exception;

public class RemoteServiceUnavailableException extends RuntimeException {
    public RemoteServiceUnavailableException(String methodKey) {
        super("Remote service unavailable: " + methodKey);
    }
}
