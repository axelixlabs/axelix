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
package com.axelixlabs.axelix.master.autoconfiguration.logging;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.logging.AxelixStructuredLoggingEnvironmentPostProcessor.AXELIX_MASTER_ENVIRONMENT;
import static com.axelixlabs.axelix.master.autoconfiguration.logging.AxelixStructuredLoggingEnvironmentPostProcessor.AXELIX_MASTER_LOGGING_JSON;
import static com.axelixlabs.axelix.master.autoconfiguration.logging.AxelixStructuredLoggingEnvironmentPostProcessor.SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.logging.AxelixStructuredLoggingEnvironmentPostProcessor.SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AxelixStructuredLoggingEnvironmentPostProcessor}.
 *
 * @author Dmitry Mazurov
 */
class AxelixStructuredLoggingEnvironmentPostProcessorTest {

    private final AxelixStructuredLoggingEnvironmentPostProcessor postProcessor =
            new AxelixStructuredLoggingEnvironmentPostProcessor();

    private final SpringApplication application = new SpringApplication();

    @Test
    void shouldRunAfterConfigDataEnvironmentPostProcessor() {
        // when. // then.
        assertThat(postProcessor.getOrder())
                .isEqualTo(Ordered.LOWEST_PRECEDENCE)
                .isGreaterThan(ConfigDataEnvironmentPostProcessor.ORDER);
    }

    @Test
    void shouldDoNothingWhenPropertyIsAbsent() {
        // given.
        MockEnvironment environment = new MockEnvironment();

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY))
                .isNull();
        assertThat(environment.getProperty(SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY))
                .isNull();
    }

    @Test
    void shouldDoNothingWhenFalseEvenIfEnvironmentIsSet() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_LOGGING_JSON, "false");
        environment.setProperty(AXELIX_MASTER_ENVIRONMENT, "production");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY))
                .isNull();
        assertThat(environment.getProperty(SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY))
                .isNull();
    }

    @Test
    void shouldForwardEcsToStructuredFormatConsoleWhenTrue() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_LOGGING_JSON, "true");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY))
                .isEqualTo("ecs");
    }

    @Test
    void shouldNotOverrideAnExplicitlySetStructuredFormatConsoleProperty() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_LOGGING_JSON, "true");
        environment.setProperty(SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY, "logstash");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_STRUCTURED_FORMAT_CONSOLE_PROPERTY))
                .isEqualTo("logstash");
    }

    @Test
    void shouldForwardEnvironmentToEcsServiceEnvironmentPropertyWhenTrue() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_LOGGING_JSON, "true");
        environment.setProperty(AXELIX_MASTER_ENVIRONMENT, "production");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY))
                .isEqualTo("production");
    }

    @Test
    void shouldLeaveEcsServiceEnvironmentUnsetWhenEnvironmentPropertyIsAbsent() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_LOGGING_JSON, "true");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY))
                .isNull();
    }

    @Test
    void shouldNotOverrideAnExplicitlySetEcsServiceEnvironmentProperty() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_LOGGING_JSON, "true");
        environment.setProperty(AXELIX_MASTER_ENVIRONMENT, "production");
        environment.setProperty(SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY, "staging");

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_STRUCTURED_ECS_SERVICE_ENVIRONMENT_PROPERTY))
                .isEqualTo("staging");
    }
}
