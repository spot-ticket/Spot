package com.example.Spot.global.infrastructure.config.security;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        // CSRF disable
        http
                .csrf(auth -> auth.disable());

        // From 로그인 방식 disable
        http
                .formLogin(auth -> auth.disable());

        // Http basic 인증 방식 disable
        http
                .httpBasic(auth -> auth.disable());

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        // 경로별 인가 작업
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("OPTIONS", "/**", "/actuator/**").permitAll()
                        // 누구나 접근 가능 (로그인, 회원가입, 토큰 갱신, 가게 조회, 카테고리 조회)
                        .requestMatchers("/api/login", "/", "/api/join", "api/auth/me", "/api/auth/refresh", "/swagger-ui/*", "v3/api-docs", "/v3/api-docs/*",
                                "/api/stores", "/api/stores/*", "/api/stores/search", "/api/categories", "/api/categories/**").permitAll()

                        // 관리자 전용 API (MASTER, MANAGER만 접근 가능)
                        .requestMatchers("/api/admin/**").hasAnyRole("MASTER", "MANAGER")

                        // 기존 관리자 경로
                        .requestMatchers("/admin").hasAnyRole("MASTER", "MANAGER")

                        // 모든 요청: 로그인 필수
                        .anyRequest().authenticated()
                        );

                        

        // 인증/권한 실패 시 JSON 응답 반환 (302 리다이렉트 방지)
        http
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access Denied\"}");
                        })
                );

        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
        );



        return http.build();
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
        return jwt -> {
            Integer userId = Integer.valueOf(String.valueOf(jwt.getClaims().get("user_id")));
            String role = String.valueOf(jwt.getClaims().getOrDefault("role", "CUSTOMER"));

            Collection<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            CustomUserDetails principal = new CustomUserDetails(userId, role);

            return new CognitoAuthenticationToken(jwt, principal, authorities);
        };
    }
}

