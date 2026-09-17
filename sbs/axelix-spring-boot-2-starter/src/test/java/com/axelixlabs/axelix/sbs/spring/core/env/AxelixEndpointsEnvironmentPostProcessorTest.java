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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import static com.axelixlabs.axelix.sbs.spring.core.env.AxelixEndpointsEnvironmentPostProcessor.INCLUDED_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AxelixEndpointsEnvironmentPostProcessor}
 *
 * @author Marsel Semenov
 */
class AxelixEndpointsEnvironmentPostProcessorTest {

    // the ids of all @Endpoint/@RestControllerEndpoint classes in this starter
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

    private AxelixEndpointsEnvironmentPostProcessor subject;

    @BeforeEach
    void setUp() {
        subject = new AxelixEndpointsEnvironmentPostProcessor();
    }

    @Test
    void whenUserNotExposeAnyEndpoints() {
        StandardEnvironment env = new StandardEnvironment();

        subject.postProcessEnvironment(env, new SpringApplication());
        assertThat(env.getProperty(INCLUDED_PROPERTY).split(",")).containsExactlyInAnyOrderElementsOf(ENDPOINTS);
    }

    @Test
    void whenUserExposedHisOwnEndpoints() {
        StandardEnvironment env = new StandardEnvironment();

        Map<String, Object> userProvidedEndpoints =
                Map.of(INCLUDED_PROPERTY, List.of("test-endpoint", "test-endpoint1"));
        env.getPropertySources().addFirst(new MapPropertySource("defaultProperties", userProvidedEndpoints));

        subject.postProcessEnvironment(env, new SpringApplication());

        List<String> expected = new ArrayList<>(List.of("test-endpoint", "test-endpoint1"));
        expected.addAll(ENDPOINTS);
        assertThat(env.getProperty(INCLUDED_PROPERTY).split(",")).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void whenUserExposedAllEndpoints() {
        StandardEnvironment env = new StandardEnvironment();

        Map<String, Object> userProvidedEndpoints = Map.of(INCLUDED_PROPERTY, "*");
        env.getPropertySources().addFirst(new MapPropertySource("defaultProperties", userProvidedEndpoints));

        subject.postProcessEnvironment(env, new SpringApplication());
        assertThat(env.getProperty(INCLUDED_PROPERTY)).isEqualTo("*");
        assertThat(env.getPropertySources().contains("axelix")).isFalse();
    }
}
