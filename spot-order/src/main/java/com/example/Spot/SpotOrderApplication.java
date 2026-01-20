package com.example.Spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpotOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotOrderApplication.class, args);
    }

}
