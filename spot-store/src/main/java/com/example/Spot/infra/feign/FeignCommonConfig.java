//package com.example.Spot.infra.feign;
//
//import feign.RequestInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import feign.codec.ErrorDecoder;
//
//@Configuration
//public class FeignCommonConfig {
//
//    @Bean
//    public RequestInterceptor feignHeaderRelayInterceptor() {
//        return new FeignHeaderRelayInterceptor();
//    }
//
//    @Bean
//    public ErrorDecoder errorDecoder() {
//        return new RemoteErrorDecoder();
//    }
//
//
//
//
//}
