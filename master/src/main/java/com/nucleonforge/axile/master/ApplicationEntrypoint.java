package com.nucleonforge.axile.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The master entrypoint.
 *
 * @author Mikhail Polivakha
 */
@SpringBootApplication
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            CompositeDiscoveryClientAutoConfiguration.class,
            SimpleDiscoveryClientAutoConfiguration.class
        })
@EnableScheduling
public class ApplicationEntrypoint {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationEntrypoint.class, args);
    }
}
