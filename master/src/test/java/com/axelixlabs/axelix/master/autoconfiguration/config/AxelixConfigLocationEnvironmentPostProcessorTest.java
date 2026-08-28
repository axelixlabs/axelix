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

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.config.AxelixConfigLocationEnvironmentPostProcessor.AXELIX_MASTER_CONFIG_LOCATION;
import static com.axelixlabs.axelix.master.autoconfiguration.config.AxelixConfigLocationEnvironmentPostProcessor.SPRING_CONFIG_ADDITIONAL_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AxelixConfigLocationEnvironmentPostProcessor}.
 *
 * @author Mikhail Polivakha
 */
class AxelixConfigLocationEnvironmentPostProcessorTest {

    private final AxelixConfigLocationEnvironmentPostProcessor postProcessor =
            new AxelixConfigLocationEnvironmentPostProcessor();

    private final SpringApplication application = new SpringApplication();

    @Test
    void shouldRunBeforeConfigDataEnvironmentPostProcessor() {
        // when. // then.
        assertThat(postProcessor.getOrder())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE)
                .isLessThan(ConfigDataEnvironmentPostProcessor.ORDER);
    }

    @Test
    void shouldDoNothingWhenPropertyIsAbsent() {
        // given.
        MockEnvironment environment = new MockEnvironment();

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_CONFIG_ADDITIONAL_LOCATION)).isNull();
    }

    @Test
    void shouldDoNothingWhenPropertyIsBlank() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_CONFIG_LOCATION, "   ");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_CONFIG_ADDITIONAL_LOCATION)).isNull();
    }

    @Test
    void shouldForwardLocationToSpringConfigAdditionalLocation() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_CONFIG_LOCATION, "file:/etc/axelix/master.yaml");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_CONFIG_ADDITIONAL_LOCATION))
                .isEqualTo("file:/etc/axelix/master.yaml");
    }

    @Test
    void shouldForwardCommaSeparatedLocationsVerbatim() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(
                AXELIX_MASTER_CONFIG_LOCATION, "optional:file:/etc/axelix/master.yaml,file:/opt/axelix/override.yaml");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_CONFIG_ADDITIONAL_LOCATION))
                .isEqualTo("optional:file:/etc/axelix/master.yaml,file:/opt/axelix/override.yaml");
    }
}
