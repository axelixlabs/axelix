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
package com.axelixlabs.axelix.sbs.spring.core.env;

/**
 * This enum contains custom descriptions for property sources.
 *
 * @author Sergey Cherkasov
 */
public enum PropertySourceCustomDescription {

    // AxelixPropertySource
    AXELIX_PROPERTY_SOURCE_NAME(
            "A custom {@link MapPropertySource} implementation used to hold mutable property values, managed dynamically during application runtime, and having the highest priority"),

    // Server Ports
    SERVER_PORTS(
            "Contains the 'server.port' property from 'application.properties/yaml', which defines the web server port (8080 by default)."),

    // StandardEnvironment
    SYSTEM_PROPERTIES(
            "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'"),
    SYSTEM_ENVIRONMENT(
            "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application*.properties/yaml'"),

    // Application
    APPLICATION_INFO(
            "Contains application metadata extracted from the 'MANIFEST.MF' file and core Spring Boot properties 'spring.application.*'"),

    // TODO: simplify the description here. It is not true that the config file is necessarily loaded from the
    // classpath.
    APPLICATION_PROPERTIES(
            "Contains properties from the 'application*.properties/yaml' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources."),

    // CommandLinePropertySource
    COMMAND_LINE_ARGS("Contains properties from the command-line arguments passed to the application at startup"),
    NON_OPTION_ARGS(
            "Contains 'non-option' command-line arguments—that is, arguments passed without the '--' or '-' prefixes"),

    // StandardServletEnvironment
    SERVLET_CONTEXT_INIT_PARAMS(
            "Contains the initialization parameters of the 'ServletContext', defined in 'web.xml' or set via 'ServletContext.setInitParameter()', and has higher priority than properties in 'jndiProperties' and 'StandardEnvironment'"),
    SERVLET_CONFIG_INIT_PARAMS(
            "Contains the initialization parameters (init-params) from 'web.xml' for a specific 'ServletConfig' and has higher priority than properties in 'servletContextInitParams' and 'StandardEnvironment'"),
    JNDI_PROPERTIES(
            "Contains properties from Java Naming and Directory Interface resources configured in the application server and has higher priority than properties in 'StandardEnvironment'"),

    // HostInfoEnvironmentPostProcessor
    SPRING_CLOUD_CLIENT_HOST_INFO(
            "Contains information about the client host for discovering and identifying instances in the cluster"),

    // BootstrapApplicationListener
    SPRING_CLOUD_DEFAULT_PROPERTIES(
            "Contains default configuration values provided by 'Spring Cloud' components, used unless they are overridden by 'bootstrap.properties/yaml' settings or properties defined in the 'StandardEnvironment'"),
    BOOTSTRAP(
            "Contains configuration loaded from 'bootstrap.properties/yaml' and initialized before the 'ApplicationContext', providing early-stage settings"),

    // ContextRefresher
    REFRESH_ARGS(
            "Contains arguments passed during a context refresh triggered by Spring Cloud’s ContextRefresher. Used to propagate dynamic configuration updates at runtime"),
    DEFAULT_PROPERTIES(
            "Contains default property values registered via 'SpringApplication.setDefaultProperties()' and has the lowest priority among properties added in code."),

    // RandomValuePropertySource
    RANDOM("Contains dynamically generated random values for placeholders like ${random.*}");

    private final String description;

    PropertySourceCustomDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
