package com.nucleonforge.axile.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.nucleonforge.axile.master.service.discovery.DiscoveryConfig;

/**
 * The master entrypoint.
 *
 * @author Mikhail Polivakha
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(DiscoveryConfig.class)
@EnableScheduling
public class ApplicationEntrypoint {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationEntrypoint.class, args);
    }
}
