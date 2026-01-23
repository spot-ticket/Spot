package com.example.Spot.global.feign.config;

import java.util.Enumeration;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;


public class FeignHeaderRelayInterceptor implements RequestInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (!(ra instanceof ServletRequestAttributes attrs)) {
            return;
        }

        HttpServletRequest request = attrs.getRequest();

        // 1) Authorization은 "있으면 반드시" 전달 (이게 없으면 store는 401이 정상)
        relaySingle(request, template, HEADER_AUTHORIZATION);

    }

    private void relaySingle(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return;
        }

        // Feign이 같은 헤더를 누적해서 붙이는 경우를 막기 위해 먼저 제거 후 1개만 세팅
        template.headers().remove(headerName);
        template.header(headerName, value);
    }

    // 필요할 때만 쓰고, 평소엔 주석 처리 추천
    @SuppressWarnings("unused")
    private void logHeaders(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            System.out.println("[INCOMING HEADER] " + n + "=" + request.getHeader(n));
        }
    }
}
