package com.example.Spot.global.feign.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

public class FeignHeaderRelayInterceptor implements RequestInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    private static final Logger LOGGER = LoggerFactory.getLogger(FeignHeaderRelayInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            LOGGER.debug("[FeignRelay] RequestContextHolder attrs is null (non-http thread). skip");
            return;
        }

        HttpServletRequest request = attrs.getRequest();

        relayHeader(request, template, HEADER_AUTHORIZATION);
    }

    private void relayHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            if (HEADER_AUTHORIZATION.equalsIgnoreCase(headerName)) {
                LOGGER.warn("[FeignRelay] Authorization header missing");
            } else {
                LOGGER.debug("[FeignRelay] {} header missing", headerName);
            }
            return;
        }
        template.header(headerName, value);

        if (HEADER_AUTHORIZATION.equalsIgnoreCase(headerName)) {
            LOGGER.info("[FeignRelay] Authorization relayed: {}", maskBearer(value));
        } else {
            LOGGER.debug("[FeignRelay] {} relayed: {}", headerName, value);
        }
    }

    private String maskBearer(String raw) {
        String v = raw.trim();
        if (!v.regionMatches(true, 0, "Bearer", 0, "Bearer".length())) {
            return maskAny(v);
        }

        int spaceIdx = v.indexOf(' ');
        if (spaceIdx < 0 || spaceIdx == v.length() - 1) {
            return "Bearer <empty>";
        }

        String token = v.substring(spaceIdx + 1).trim();
        return "Bearer " + maskToken(token);
    }

    private String maskAny(String v) {
        if (v.length() <= 8) {
            return "****";
        }
        return v.substring(0, 4) + "..." + v.substring(v.length() - 4);
    }

    private String maskToken(String token) {
        if (token.length() <= 12) {
            return "****";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 6);
    }
}
