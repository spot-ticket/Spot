package com.example.Spot.global.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.Spot.infra.auth.jwt.JWTFilter;
import com.example.Spot.infra.auth.jwt.JWTUtil;
import com.example.Spot.infra.auth.jwt.LoginFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    //JWTUtil 주입
    private final JWTUtil jwtUtil;
//    // TokenService 주입
//    private final TokenService tokenService;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        LoginFilter loginFilter =
                new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/login");

        // CSRF disable
        http
                .csrf(auth -> auth.disable());

        // From 로그인 방식 disable
        http
                .formLogin(auth -> auth.disable());

        // Http basic 인증 방식 disable
        http
                .httpBasic(auth -> auth.disable());

        // 경로별 인가 작업
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("OPTIONS", "/**").permitAll()
                        // 누구나 접근 가능 (로그인, 회원가입, 토큰 갱신, 가게 조회, 카테고리 조회)
                        .requestMatchers("/api/login", "/", "/api/join", "/api/auth/refresh", "/swagger-ui/*", "v3/api-docs", "/v3/api-docs/*",
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

        http.addFilterBefore(
                new JWTFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
        );

        //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        http.addFilterAt(
                loginFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}

