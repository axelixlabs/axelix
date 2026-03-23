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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * This enum holds the name of the property source along with a custom description
 *
 * @author Sergey Cherkasov
 */
public enum PropertySourceDescription {

    // TODO: Remove this property source if context reload is not ported to the Spring Boot 2 starter.
    /*  // AxelixPropertySource
        AXELIX_PROPERTY_SOURCE_NAME(
                AxelixPropertySource.AXELIX_PROPERTY_SOURCE_NAME,
                "A custom {@link MapPropertySource} implementation used to hold mutable property values, managed dynamically during application runtime, and having the highest priority"),
    */

    // Server Ports
    SERVER_PORTS("server.ports", PropertySourceCustomDescription.SERVER_PORTS.getDescription()),

    // StandardEnvironment
    SYSTEM_PROPERTIES(
            StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.SYSTEM_PROPERTIES.getDescription()),
    SYSTEM_ENVIRONMENT(
            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            PropertySourceCustomDescription.SYSTEM_ENVIRONMENT.getDescription()),

    // Application Info
    APPLICATION_INFO("applicationInfo", PropertySourceCustomDescription.APPLICATION_INFO.getDescription()),

    // Application Properties
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

    /**
     * Matches Spring's config resource property source names to extract the file name and location.
     * <p>
     * Example: "Config resource 'class path resource [application-dev.properties]' via location 'optional:classpath:/'"
     * Example: "Config resource 'file [/etc/app/application-prod.yaml]' via location 'optional:file:/etc/app/'"
     * <p>
     * Well, yes, this approach is not that reliable, and we know that. However, the problem is that the name of the given
     * {@link org.springframework.core.env.PropertySource}, especially those that we care about, i.e.
     * <ol>
     *     <li>{@link org.springframework.boot.env.OriginTrackedMapPropertySource})</li>
     *     <li>{@link org.springframework.core.io.support.ResourcePropertySource})</li>
     * </ol>
     *
     * contains all the information required for us to present it in a user-friendly way. And, apparently, it is the easiest
     * approach out there to get this info, since all the PropertySources above loose all the information about the underlying
     * {@link org.springframework.core.io.Resource}, it is just gone at runtime. We can of course try to overcome this by writing
     * custom machinery around PropertySources, but that just does not justify the engineering effort.
     */
    private static final Pattern CONFIG_RESOURCE_PATTERN =
            Pattern.compile("Config resource '(?:class path resource|file) \\[([^]]+)]' via location '([^']+)'");

    private final String sourceName;
    private final String description;

    PropertySourceDescription(String sourceName, String description) {
        this.sourceName = sourceName;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PropertySourceDisplayData resolveDisplayData(String sourceName) {
        if (sourceName.startsWith("Config resource")) {
            Matcher matcher = CONFIG_RESOURCE_PATTERN.matcher(sourceName);
            if (matcher.find()) {
                String displayName = Paths.get(matcher.group(1)).getFileName().toString();
                String description = String.format(
                        "Properties that are loaded from %s located in %s", displayName, matcher.group(2));
                return new PropertySourceDisplayData(displayName, description);
            }
        }
        return new PropertySourceDisplayData(sourceName, getDescriptionBySourceName(sourceName));
    }

    private static @Nullable String getDescriptionBySourceName(String sourceName) {
        return findBySourceName(sourceName)
                .map(PropertySourceDescription::getDescription)
                .orElse(null);
    }

    private static Optional<PropertySourceDescription> findBySourceName(String sourceName) {
        return Arrays.stream(values())
                .filter(desc -> desc.sourceName.equals(sourceName) || sourceName.startsWith(desc.sourceName))
                .findFirst();
    }
}
