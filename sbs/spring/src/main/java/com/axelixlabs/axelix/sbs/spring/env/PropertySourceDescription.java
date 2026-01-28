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
package com.axelixlabs.axelix.sbs.spring.env;

import java.util.Arrays;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.axelixlabs.axelix.sbs.spring.properties.AxelixPropertySource;

/**
 * This enum holds the name of the property source along with a custom description
 *
 * @author Sergey Cherkasov
 */
public enum PropertySourceDescription {

    // AxelixPropertySource
    AXELIX_PROPERTY_SOURCE_NAME(
            AxelixPropertySource.AXELIX_PROPERTY_SOURCE_NAME,
            "A custom {@link MapPropertySource} implementation used to hold mutable property values, managed dynamically during application runtime, and having the highest priority"),

    SERVER_PORTS(
            "server.ports",
            "Contains the 'server.port' property from 'application.properties/yaml', which defines the web server port (8080 by default)."),

    // StandardEnvironment
    SYSTEM_PROPERTIES(
            StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
            "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'"),
    SYSTEM_ENVIRONMENT(
            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application*.properties/yaml'"),

    APPLICATION_INFO(
            "applicationInfo",
            "Contains application metadata extracted from the 'MANIFEST.MF' file and core Spring Boot properties 'spring.application.*'"),

    APPLICATION_PROPERTIES(
            "Config resource",
            "Contains properties from the 'application*.properties/yaml' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources."),

    // CommandLinePropertySource
    COMMAND_LINE_ARGS(
            CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME,
            "Contains properties from the command-line arguments passed to the application at startup"),
    NON_OPTION_ARGS(
            CommandLinePropertySource.DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME,
            "Contains 'non-option' command-line arguments—that is, arguments passed without the '--' or '-' prefixes"),

    // StandardServletEnvironment
    SERVLET_CONTEXT_INIT_PARAMS(
            StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME,
            "Contains the initialization parameters of the 'ServletContext', defined in 'web.xml' or set via 'ServletContext.setInitParameter()', and has higher priority than properties in 'jndiProperties' and 'StandardEnvironment'"),
    SERVLET_CONFIG_INIT_PARAMS(
            StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME,
            "Contains the initialization parameters (init-params) from 'web.xml' for a specific 'ServletConfig' and has higher priority than properties in 'servletContextInitParams' and 'StandardEnvironment'"),
    JNDI_PROPERTIES(
            StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME,
            "Contains properties from Java Naming and Directory Interface resources configured in the application server and has higher priority than properties in 'StandardEnvironment'"),

    // HostInfoEnvironmentPostProcessor
    SPRING_CLOUD_CLIENT_HOST_INFO(
            "springCloudClientHostInfo",
            "Contains information about the client host for discovering and identifying instances in the cluster"),

    // BootstrapApplicationListener
    SPRING_CLOUD_DEFAULT_PROPERTIES(
            BootstrapApplicationListener.DEFAULT_PROPERTIES,
            "Contains default configuration values provided by 'Spring Cloud' components, used unless they are overridden by 'bootstrap.properties/yaml' settings or properties defined in the 'StandardEnvironment'"),
    BOOTSTRAP(
            BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME,
            "Contains configuration loaded from 'bootstrap.properties/yaml' and initialized before the 'ApplicationContext', providing early-stage settings"),

    // ContextRefresher
    REFRESH_ARGS(
            "refreshArgs",
            "Contains arguments passed during a context refresh triggered by Spring Cloud’s ContextRefresher. Used to propagate dynamic configuration updates at runtime"),
    DEFAULT_PROPERTIES(
            "defaultProperties",
            "Contains default property values registered via 'SpringApplication.setDefaultProperties()' and has the lowest priority among properties added in code."),

    // RandomValuePropertySource
    RANDOM(
            RandomValuePropertySource.RANDOM_PROPERTY_SOURCE_NAME,
            "Contains dynamically generated random values for placeholders like ${random.*}");

    private final String sourceName;
    private final String description;

    PropertySourceDescription(String sourceName, String description) {
        this.sourceName = sourceName;
        this.description = description;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getDescription() {
        return description;
    }

    private static Optional<PropertySourceDescription> findBySourceName(String sourceName) {
        return Arrays.stream(values())
                .filter(desc -> desc.sourceName.equals(sourceName) || sourceName.startsWith(desc.sourceName))
                .findFirst();
    }

    public static @Nullable String getDescriptionBySourceName(String sourceName) {
        return findBySourceName(sourceName)
                .map(PropertySourceDescription::getDescription)
                .orElse(null);
    }
}
