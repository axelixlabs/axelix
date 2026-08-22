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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.AXELIX_PREFIX;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.EXTERNAL_CONFIG_OPTION;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.MAPPED_PROPERTIES;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.PROPERTY_SOURCE_NAME;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.SPRING_CLOUD_CONFIG_VALUE;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.SPRING_CONFIG_IMPORT_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.SPRING_PREFIX;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.WAY_OF_CONFIG_IMPORT;

/**
 * An {@link EnvironmentPostProcessor} responsible for dynamically activating and
 * configuring external configuration providers for Axelix Master based on application properties.
 *
 * <p>If the property {@code axelix.master.external-config.option} is set to {@code spring-cloud-config},
 * this processor injects {@code spring.config.import=configserver:} and {@code spring.cloud.config.enabled=true}
 * into the environment.</p>
 *
 * @author Vyacheslav Yanin
 * @see AxelixConfigConstant
 */
public class ExternalConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Load application.yaml early to read flags before SB's main loop
        loadEarlyApplicationYaml(environment);

        String option = environment.getProperty(EXTERNAL_CONFIG_OPTION.value());

        if (SPRING_CLOUD_CONFIG_VALUE.value().equalsIgnoreCase(option)) {
            var targetProperties = new HashMap<String, Object>();

            targetProperties.put(SPRING_CONFIG_IMPORT_PROPERTY.value(), WAY_OF_CONFIG_IMPORT.value());
            targetProperties.put(AxelixConfigConstant.SPRING_CLOUD_CONFIG_ENABLED_PROPERTY.value(), true);

            fillTargetProperties(environment, targetProperties);

            var propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME.value(), targetProperties);
            environment.getPropertySources().addFirst(propertySource);
        }
    }

    private void loadEarlyApplicationYaml(ConfigurableEnvironment environment) {
        try {
            var resource = new ClassPathResource("application.yaml");
            if (resource.exists()) {
                List<PropertySource<?>> loaded = new YamlPropertySourceLoader().load("earlyApplicationYaml", resource);
                for (PropertySource<?> source : loaded) {
                    environment.getPropertySources().addLast(source);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load early application.yaml", e);
        }
    }

    private void fillTargetProperties(ConfigurableEnvironment environment, HashMap<String, Object> targetProperties) {
        for (String prop : MAPPED_PROPERTIES) {
            String value = environment.getProperty(AXELIX_PREFIX.value() + prop);
            if (value != null && !value.isBlank()) {
                targetProperties.put(SPRING_PREFIX.value() + prop, value);
            }
        }
    }

    /**
     * @return {@link Ordered#HIGHEST_PRECEDENCE} to ensure this processor executes before
     *         native Spring Boot configuration importers inspect the environment.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
