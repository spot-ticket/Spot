package com.example.Spot.global.feign.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignHeaderRelayInterceptor implements RequestInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return;
        }

        HttpServletRequest request = attrs.getRequest();

        relayHeader(request, template, HEADER_AUTHORIZATION);
    }

    private void relayHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            template.header(headerName, value);
        }
    }
}
