/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixConfigurationsPropertiesEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixEnvironmentEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixHeapDumpEndpointAutoConfiguration;

/**
 * Minimal Spring Boot application used exclusively for testing this application.
 *
 * <p>This class provides a {@link SpringBootApplication}
 * context for integration tests annotated with {@code @SpringBootTest}.
 *
 * <p>Auto-configurations excluded:
 * <ul>
 *   <li>{@link AxelixEnvironmentEndpointAutoConfiguration} and
 *       {@link AxelixConfigurationsPropertiesEndpointAutoConfiguration} - their
 *       auto-config registers a {@code CustomRequiredAuthorityCheckService}
 *       that bypasses authentication. Endpoint tests use the real
 *       {@code RequiredAuthorityCheckService} registered by the shared test
 *       configuration.</li>
 *   <li>{@link AxelixHeapDumpEndpointAutoConfiguration} - heap dump endpoint
 *       is not covered by endpoint integration tests.</li>
 * </ul>
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@SpringBootApplication(
        exclude = {
            AxelixConfigurationsPropertiesEndpointAutoConfiguration.class,
            AxelixEnvironmentEndpointAutoConfiguration.class,
            AxelixHeapDumpEndpointAutoConfiguration.class
        })
@EnableCaching
@EnableFeignClients
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
