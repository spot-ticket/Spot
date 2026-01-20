package com.example.Spot.global.infrastructure.client.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

import java.util.UUID;

public class RequestIdInterceptor implements RequestInterceptor {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public void apply(RequestTemplate template) {
        String requestId = MDC.get(HEADER_REQUEST_ID);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        template.header(HEADER_REQUEST_ID, requestId);
    }
}
