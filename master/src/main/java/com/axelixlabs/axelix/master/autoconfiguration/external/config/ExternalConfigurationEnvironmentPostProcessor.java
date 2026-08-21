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
package com.axelixlabs.axelix.master.autoconfiguration.external.config;

import java.util.HashMap;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * An {@link EnvironmentPostProcessor} responsible for dynamically activating and
 * configuring external configuration providers for Axelix Master based on application properties.
 *
 * <p>If the property {@code axelix.master.external-config.option} is set to {@code spring-cloud-config},
 * this processor injects {@code spring.config.import=configserver:} and {@code spring.cloud.config.enabled=true}
 * into the environment.</p>
 *
 * @author Vyacheslav Yanin
 */
public class ExternalConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String EXTERNAL_CONFIG_OPTION = "axelix.master.external-config.option";

    public static final String SPRING_CLOUD_CONFIG_VALUE = "spring-cloud-config";

    public static final String PROPERTY_SOURCE_NAME = "axelixExternalConfigProperties";

    public static final String SPRING_CONFIG_IMPORT_PROPERTY = "spring.config.import";

    public static final String SPRING_CLOUD_CONFIG_ENABLED_PROPERTY = "spring.cloud.config.enabled";

    public static final String WAY_OF_CONFIG_IMPORT = "configserver:";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String option = environment.getProperty(EXTERNAL_CONFIG_OPTION);

        if (SPRING_CLOUD_CONFIG_VALUE.equalsIgnoreCase(option)) {
            var targetProperties = new HashMap<String, Object>();

            targetProperties.put(SPRING_CONFIG_IMPORT_PROPERTY, WAY_OF_CONFIG_IMPORT);
            targetProperties.put(SPRING_CLOUD_CONFIG_ENABLED_PROPERTY, true);

            var propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, targetProperties);
            environment.getPropertySources().addFirst(propertySource);
        }
    }

    /**
     * @return {@code ConfigDataEnvironmentPostProcessor.Order + 1} to ensure this processor executes after
     *         {@code ConfigDataEnvironmentPostProcessor}.
     */
    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
