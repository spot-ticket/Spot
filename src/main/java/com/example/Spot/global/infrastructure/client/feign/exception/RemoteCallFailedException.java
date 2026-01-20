package com.example.Spot.global.infrastructure.client.feign.exception;

public class RemoteCallFailedException extends RuntimeException {
    public RemoteCallFailedException(String methodKey, int status) {
        super("Remote call failed: " + methodKey + " status=" + status);
    }
}
