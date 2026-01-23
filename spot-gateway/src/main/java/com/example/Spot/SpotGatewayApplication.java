package com.example.Spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.Spot")
public class SpotGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotGatewayApplication.class, args);
    }

}
