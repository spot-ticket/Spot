package com.example.Spot.infra.feign.exception;

public class RemoteNotFoundException extends RuntimeException {
    public RemoteNotFoundException(String methodKey) {
        super("Remote resource not found: " + methodKey);
    }
}
