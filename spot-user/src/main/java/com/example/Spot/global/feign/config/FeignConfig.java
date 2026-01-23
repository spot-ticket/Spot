package com.example.Spot.global.feign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;



@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignHeaderRelayInterceptor() {
        return new FeignHeaderRelayInterceptor();
    }
}
