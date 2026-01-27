package com.example.Spot.global.feign.config.exception;

public class RemoteNotFoundException extends RuntimeException {
    public RemoteNotFoundException(String methodKey) {
        super("Remote resource not found: " + methodKey);
    }
}
