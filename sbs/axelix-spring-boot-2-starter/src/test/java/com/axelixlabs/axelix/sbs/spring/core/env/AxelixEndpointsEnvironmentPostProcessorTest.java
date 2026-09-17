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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AxelixEndpointsEnvironmentPostProcessor}
 *
 * @author Marsel Semenov
 */
class AxelixEndpointsEnvironmentPostProcessorTest {

    private final AxelixEndpointsEnvironmentPostProcessor processor = new AxelixEndpointsEnvironmentPostProcessor();
    private static final String PROPERTY_NAME = "management.endpoints.web.exposure.include";
    // the ids of all @Endpoint/@RestControllerEndpoint classes in this starter, in the sorted order
    // the post-processor emits them
    private static final List<String> ENDPOINTS = List.of(
            "axelix-beans",
            "axelix-caches",
            "axelix-conditions",
            "axelix-configprops",
            "axelix-dependencies",
            "axelix-details",
            "axelix-env",
            "axelix-gc",
            "axelix-heap-dump",
            "axelix-loggers",
            "axelix-metadata",
            "axelix-metrics",
            "axelix-scheduled-tasks",
            "axelix-thread-dump");

    @Test
    void whenUserNotExposeAnyEndpoints() {
        StandardEnvironment env = new StandardEnvironment();

        processor.postProcessEnvironment(env, new SpringApplication());
        assertThat(env.getProperty(PROPERTY_NAME)).isEqualTo("health," + String.join(",", ENDPOINTS));
    }

    @Test
    void whenUserExposedHisOwnEndpoints() {
        StandardEnvironment env = new StandardEnvironment();

        Map<String, Object> userProvidedEndpoints = Map.of(PROPERTY_NAME, List.of("test-endpoint", "test-endpoint1"));
        env.getPropertySources().addFirst(new MapPropertySource("defaultProperties", userProvidedEndpoints));

        processor.postProcessEnvironment(env, new SpringApplication());
        assertThat(env.getProperty(PROPERTY_NAME))
                .isEqualTo("test-endpoint,test-endpoint1," + String.join(",", ENDPOINTS));
    }
}
