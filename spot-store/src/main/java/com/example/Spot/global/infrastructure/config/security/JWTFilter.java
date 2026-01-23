package com.example.Spot.global.infrastructure.config.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.Spot.global.common.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class JWTFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTFilter.class);

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String method = request.getMethod();
        final String uri = request.getRequestURI();
        final String authorization = request.getHeader("Authorization");

        // ✅ 1) 필터 타는지 무조건 보이게
        LOGGER.info("[JWTFilter] hit {} {} hasAuthHeader={}", method, uri, authorization != null);

        // ✅ 2) Bearer 없으면 통과 (permitAll이면 컨트롤러 principal=null 정상)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            LOGGER.info("[JWTFilter] no bearer -> pass through {} {}", method, uri);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();
        LOGGER.info("[JWTFilter] bearer token length={} {} {}", token.length(), method, uri);

        try {
            if (jwtUtil.isExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String type = jwtUtil.getTokenType(token);
            if (type == null || !"access".equals(type)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Integer userId = jwtUtil.getUserId(token);
            Role role = jwtUtil.getRole(token);

            if (userId == null || role == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            CustomUserDetails principal = new CustomUserDetails(userId, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            LOGGER.error("[JWTFilter] exception while validating token {} {}", method, uri, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private Collection<GrantedAuthority> toAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String r : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
        }
        return authorities;
    }
}
