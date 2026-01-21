package com.example.Spot.global.infrastructure.client.feign.exception;

public class RemoteConflictException extends RuntimeException {
    public RemoteConflictException(String methodKey) {
        super("Remote conflict: " + methodKey);
    }
}
