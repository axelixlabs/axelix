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
import java.util.Map;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Exposes default endpoints, that Axelix need
 *
 * @author Marsel Semenov
 * @author Andrey Kotov
 */
public class AxelixEndpointsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String INCLUDED_PROPERTY = "management.endpoints.web.exposure.include";

    private final AxelixEndpointDiscovery endpointDiscovery;

    public AxelixEndpointsEnvironmentPostProcessor() {
        this.endpointDiscovery = new AxelixEndpointDiscovery();
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Set<String> current = Binder.get(environment)
                .bind(INCLUDED_PROPERTY, Bindable.setOf(String.class))
                .orElse(Collections.emptySet());

        if (current.contains("*")) {
            return;
        }

        Set<String> merged = new LinkedHashSet<>();
        if (current.isEmpty()) {
            // the property is unset, so keep Spring Boot's default exposure ("health"),
            // which the property source added below would otherwise override
            merged.add("health");
        }
        current.stream().map(String::strip).filter(s -> !s.isEmpty()).forEach(merged::add);

        endpointDiscovery.discoverEndpointIds(environment).stream()
                .map(EndpointId::toString)
                .forEach(merged::add);
        environment
                .getPropertySources()
                .addFirst(new MapPropertySource("axelix", Map.of(INCLUDED_PROPERTY, String.join(",", merged))));
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
