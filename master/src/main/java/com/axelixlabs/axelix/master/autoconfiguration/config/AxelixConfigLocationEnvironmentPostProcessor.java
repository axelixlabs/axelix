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
package com.axelixlabs.axelix.master.autoconfiguration.config;

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Gives Axelix Master a native, {@code axelix.master.*}-prefixed way to point at an external configuration file,
 * so users do not have to reach for the raw Spring Boot {@code spring.config.*} machinery.
 * <p>
 * {@code axelix.master.config.location}, when set, is forwarded verbatim to Spring Boot's
 * {@code spring.config.additional-location}. The value carries the full Spring Boot semantics: it may point at a
 * single file or a directory, list several comma-separated locations, and use prefixes such as {@code optional:}
 * or {@code file:}. Because it maps to the <em>additional</em> location, Master's bundled defaults still load and
 * the external file merely overrides them.
 * <p>
 * Like {@code spring.config.location}, this property is only honoured when supplied through a source that is
 * available before configuration data is loaded - command-line arguments, JVM system properties or environment
 * variables - not from within {@code application.yaml} itself.
 *
 * @author Mikhail Polivakha
 */
public class AxelixConfigLocationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String AXELIX_MASTER_CONFIG_LOCATION = "axelix.master.config.location";

    public static final String SPRING_CONFIG_ADDITIONAL_LOCATION = "spring.config.additional-location";

    private static final String PROPERTY_SOURCE_NAME = "axelixConfigLocation";

    /**
     * Runs before {@link ConfigDataEnvironmentPostProcessor} (order {@code HIGHEST_PRECEDENCE + 10}) so that the
     * {@code spring.config.additional-location} we contribute is in place by the time configuration data is
     * imported.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String location = environment.getProperty(AXELIX_MASTER_CONFIG_LOCATION);
        if (!StringUtils.hasText(location)) {
            return;
        }

        environment
                .getPropertySources()
                .addLast(new MapPropertySource(
                        PROPERTY_SOURCE_NAME, Map.of(SPRING_CONFIG_ADDITIONAL_LOCATION, location)));
    }
}
