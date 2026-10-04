package com.axelixlabs.playground.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A minimal, stateless Spring Boot 4 "API gateway" style service used as an Axelix playground.
 *
 * <p>It has NO relational database and therefore no {@code org.springframework:spring-tx} on its
 * classpath. Its purpose is to verify that the {@code axelix-spring-boot-4-starter} boots cleanly in
 * that configuration (regression coverage for GH-1708).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
