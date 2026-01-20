package com.example.Spot.global.infrastructure.client.feign.exception;

public class RemoteNotFoundException extends RuntimeException {
    public RemoteNotFoundException(String methodKey) {
        super("Remote resource not found: " + methodKey);
    }
}
