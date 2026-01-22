package com.example.Spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpotStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotStoreApplication.class, args);
    }

}
