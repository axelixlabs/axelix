package com.nucleonforge.axile.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.nucleonforge.axile.master.service.discovery.DiscoveryConfig;

/**
 * Minimal Spring Boot application used exclusively for testing this application.
 *
 * @since 17.07.2025
 * @author Nikita Kirillov
 */
@SpringBootApplication
@EnableConfigurationProperties(DiscoveryConfig.class)
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
