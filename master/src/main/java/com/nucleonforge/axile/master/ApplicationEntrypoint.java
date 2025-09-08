package com.nucleonforge.axile.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.nucleonforge.axile.master.service.discovery.DiscoveryConfig;

/**
 * The master entrypoint.
 *
 * @author Mikhail Polivakha
 */
@SpringBootApplication
@EnableConfigurationProperties(DiscoveryConfig.class)
public class ApplicationEntrypoint {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationEntrypoint.class, args);
    }
}
