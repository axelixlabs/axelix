package com.axelixlabs.playground.greeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * A Spring Boot 4 WebFlux service used as an Axelix playground.
 */
@SpringBootApplication
@EnableCaching
public class GreetingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetingServiceApplication.class, args);
    }
}
