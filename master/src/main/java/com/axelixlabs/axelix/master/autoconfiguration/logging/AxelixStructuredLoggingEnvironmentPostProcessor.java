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
package com.axelixlabs.axelix.master.autoconfiguration.logging;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Enables Spring Boot's own structured logging properties based on {@code axelix.master.logging.json.enabled},
 * before the logging system initializes. Property names follow the same {@code axelix.master.*} prefix
 * as the rest of Master's configuration.
 * <p>
 * {@code axelix.master.logging.json.enabled=true} enables ECS structured console logging - the only format
 * Master supports for now.
 * <p>
 * {@code axelix.master.environment}, when set, is forwarded to
 * {@code logging.structured.ecs.service.environment} - but only when {@code axelix.master.logging.json.enabled}
 * is also {@code true}.
 *
 * @author Dmitry Mazurov
 */
public class AxelixStructuredLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String AXELIX_MASTER_LOGGING_JSON = "axelix.master.logging.json.enabled";

    public static final String AXELIX_MASTER_ENVIRONMENT = "axelix.master.environment";

    private static final String ECS_FORMAT_ID = "ecs";

    public static final String SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY = "logging.structured.format.console";

    public static final String SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY =
            "logging.structured.ecs.service.environment";

    private static final String PROPERTY_SOURCE_NAME = "axelixStructuredLogging";

    /**
     * Runs after every explicitly-ordered {@link EnvironmentPostProcessor}, in particular
     * {@link ConfigDataEnvironmentPostProcessor} (order {@code HIGHEST_PRECEDENCE + 10}, where
     * {@code application.yaml} gets loaded): without that, an {@code axelix.master.logging.json.enabled} set there
     * wouldn't be visible yet when this class reads it below.
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(AXELIX_MASTER_LOGGING_JSON, Boolean.class, false)) {
            return;
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY, ECS_FORMAT_ID);

        String environmentName = environment.getProperty(AXELIX_MASTER_ENVIRONMENT);
        if (StringUtils.hasText(environmentName)) {
            properties.put(SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY, environmentName);
        }

        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }
}
