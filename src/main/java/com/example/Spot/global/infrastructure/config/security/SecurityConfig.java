<<<<<<<< HEAD:src/main/java/com/example/Spot/global/infrastructure/config/security/SecurityConfig.java
package com.example.Spot.global.infrastructure.config.security;

========
package com.example.Spot.global.infrastructure.config;

import com.example.Spot.infra.auth.jwt.JWTFilter;
import com.example.Spot.infra.auth.jwt.JWTUtil;
import com.example.Spot.infra.auth.jwt.LoginFilter;
import jakarta.servlet.http.HttpServletRequest;
>>>>>>>> 7404372 (chore(#0): 작업 중 상태 저장):src/main/java/com/example/Spot/global/infrastructure/config/SecurityConfig.java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
<<<<<<<< HEAD:src/main/java/com/example/Spot/global/infrastructure/config/security/SecurityConfig.java

========
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
>>>>>>>> 7404372 (chore(#0): 작업 중 상태 저장):src/main/java/com/example/Spot/global/infrastructure/config/SecurityConfig.java


@Configuration
@EnableWebSecurity
public class SecurityConfig {
<<<<<<<< HEAD:src/main/java/com/example/Spot/global/infrastructure/config/security/SecurityConfig.java
========
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    //JWTUtil 주입
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }
    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
>>>>>>>> 7404372 (chore(#0): 작업 중 상태 저장):src/main/java/com/example/Spot/global/infrastructure/config/SecurityConfig.java

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                })));

        // CSRF disable
        http
                .csrf((auth) -> auth.disable());

        // From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        // Http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        // 경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "/join","/users/**").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated());

<<<<<<<< HEAD:src/main/java/com/example/Spot/global/infrastructure/config/security/SecurityConfig.java
========

        // LoginFilter 등록
        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

>>>>>>>> 7404372 (chore(#0): 작업 중 상태 저장):src/main/java/com/example/Spot/global/infrastructure/config/SecurityConfig.java
        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
