package com.example.Spot.global.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.Spot.infra.auth.security.DevAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile({"local", "ci"})// CI도 local로 돌리면 됨
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("OPTIONS", "/**").permitAll()
                .requestMatchers(
                        "/", "/swagger-ui/**", "/v3/api-docs/**",
                        "/api/stores/**", "/api/categories/**"
                ).permitAll()
                .anyRequest().authenticated()
        );

        // 개발/CI용: 임시 principal 주입 (예: OWNER)
        http.addFilterBefore(
                new DevAuthFilter(1, "OWNER"),
                UsernamePasswordAuthenticationFilter.class
        );

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
