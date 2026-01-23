package com.example.Spot.infra.feign.exception;

public class RemoteConflictException extends RuntimeException {
    public RemoteConflictException(String methodKey) {
        super("Remote conflict: " + methodKey);
    }
}
