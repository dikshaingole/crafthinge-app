package com.crafthinge.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// scanBasePackages also picks up the shared beans in com.crafthinge.common
@SpringBootApplication(scanBasePackages = "com.crafthinge")
public class DeliveryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApplication.class, args);
    }
}
