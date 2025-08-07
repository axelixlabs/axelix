package com.nucleonforge.axile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Minimal Spring Boot application used exclusively for testing this application.
 *
 * <p>This class provides a {@link SpringBootApplication}
 * context for integration tests annotated with {@code @SpringBootTest}.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
