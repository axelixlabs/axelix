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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Exposes default endpoints, that Axelix need
 *
 * @author Marsel Semenov
 */
public class AxelixEndpointsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY = "management.endpoints.web.exposure.include";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Set<String> current = Binder.get(environment)
                .bind(PROPERTY, Bindable.setOf(String.class))
                .orElse(Collections.emptySet());

        if (current.contains("*")) {
            return;
        }

        Set<String> merged = new LinkedHashSet<>();
        current.stream().map(String::strip).filter(s -> !s.isEmpty()).forEach(merged::add);

        merged.addAll(discoverAxelixEndpointIds(environment));
        environment
                .getPropertySources()
                .addFirst(new MapPropertySource("axelix", Map.of(PROPERTY, String.join(",", merged))));
    }

    /**
     * Discovers the ids of Axelix actuator endpoints by scanning the starter's classes for
     * {@link Endpoint @Endpoint} — directly present or as a meta-annotation ({@code @RestControllerEndpoint#id}
     * aliases {@code @Endpoint#id}), so the endpoint declarations stay the single source of truth.
     */
    private static List<String> discoverAxelixEndpointIds(ConfigurableEnvironment environment) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, environment);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Endpoint.class));
        return scanner.findCandidateComponents("com.axelixlabs.axelix.sbs").stream()
                .map(candidate -> ((AnnotatedBeanDefinition) candidate)
                        .getMetadata()
                        .getAnnotations()
                        .get(Endpoint.class)
                        .getString("id"))
                .filter(id -> id.startsWith("axelix-"))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
