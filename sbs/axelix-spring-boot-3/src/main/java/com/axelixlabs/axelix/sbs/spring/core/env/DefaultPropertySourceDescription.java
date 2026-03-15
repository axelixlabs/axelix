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

import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.axelixlabs.axelix.sbs.spring.core.properties.AxelixPropertySource;

/**
 * This enum holds the name of the property source along with a custom description.
 *
 * @author Sergey Cherkasov
 */
public enum DefaultPropertySourceDescription implements PropertySourceDescription {

    // AxelixPropertySource
    AXELIX_PROPERTY_SOURCE_NAME(
            AxelixPropertySource.AXELIX_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.AXELIX_PROPERTY_SOURCE_NAME.getDescription()),

    // Server Ports
    SERVER_PORTS("server.ports", PropertySourceCustomDescription.SERVER_PORTS.getDescription()),

    // StandardEnvironment
    SYSTEM_PROPERTIES(
            StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.SYSTEM_PROPERTIES.getDescription()),
    SYSTEM_ENVIRONMENT(
            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.SYSTEM_ENVIRONMENT.getDescription()),

    // Application
    APPLICATION_INFO("applicationInfo", PropertySourceCustomDescription.APPLICATION_INFO.getDescription()),

    // classpath.
    APPLICATION_PROPERTIES("Config resource", PropertySourceCustomDescription.APPLICATION_PROPERTIES.getDescription()),

    // CommandLinePropertySource
    COMMAND_LINE_ARGS(
            CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.COMMAND_LINE_ARGS.getDescription()),
    NON_OPTION_ARGS(
            CommandLinePropertySource.DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME,
            PropertySourceCustomDescription.NON_OPTION_ARGS.getDescription()),

    // StandardServletEnvironment
    SERVLET_CONTEXT_INIT_PARAMS(
            StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.SERVLET_CONTEXT_INIT_PARAMS.getDescription()),
    SERVLET_CONFIG_INIT_PARAMS(
            StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.SERVLET_CONFIG_INIT_PARAMS.getDescription()),
    JNDI_PROPERTIES(
            StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.JNDI_PROPERTIES.getDescription()),

    // HostInfoEnvironmentPostProcessor
    SPRING_CLOUD_CLIENT_HOST_INFO(
            "springCloudClientHostInfo",
            PropertySourceCustomDescription.SPRING_CLOUD_CLIENT_HOST_INFO.getDescription()),

    // BootstrapApplicationListener
    SPRING_CLOUD_DEFAULT_PROPERTIES(
            BootstrapApplicationListener.DEFAULT_PROPERTIES,
            PropertySourceCustomDescription.SPRING_CLOUD_DEFAULT_PROPERTIES.getDescription()),
    BOOTSTRAP(
            BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.BOOTSTRAP.getDescription()),

    // ContextRefresher
    REFRESH_ARGS("refreshArgs", PropertySourceCustomDescription.REFRESH_ARGS.getDescription()),
    DEFAULT_PROPERTIES("defaultProperties", PropertySourceCustomDescription.DEFAULT_PROPERTIES.getDescription()),

    // RandomValuePropertySource
    RANDOM(
            RandomValuePropertySource.RANDOM_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.RANDOM.getDescription());

    private final String sourceName;
    private final String description;

    DefaultPropertySourceDescription(String sourceName, String description) {
        this.sourceName = sourceName;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }
}
