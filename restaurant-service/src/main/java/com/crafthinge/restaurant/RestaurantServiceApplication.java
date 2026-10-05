package com.crafthinge.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// scanBasePackages also picks up the shared beans in com.crafthinge.common
@SpringBootApplication(scanBasePackages = "com.crafthinge")
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
