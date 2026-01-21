package com.example.Spot.global.infrastructure.client.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.codec.ErrorDecoder;

@Configuration
public class FeignCommonConfig {

    @Bean
    public RequestIdInterceptor requestIdInterceptor() {
        return new RequestIdInterceptor();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new RemoteErrorDecoder();
    }




}
