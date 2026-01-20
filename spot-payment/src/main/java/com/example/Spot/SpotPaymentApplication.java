package com.example.Spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.Spot.global.feign")
public class SpotPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotPaymentApplication.class, args);
    }

}
