package com.example.Spot.global.feign.config.exception;

public class RemoteConflictException extends RuntimeException {
    public RemoteConflictException(String methodKey) {
        super("Remote conflict: " + methodKey);
    }
}
