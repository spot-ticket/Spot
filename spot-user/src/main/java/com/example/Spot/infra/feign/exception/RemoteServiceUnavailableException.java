package com.example.Spot.infra.feign.exception;

public class RemoteServiceUnavailableException extends RuntimeException {
    public RemoteServiceUnavailableException(String methodKey) {
        super("Remote service unavailable: " + methodKey);
    }
}
