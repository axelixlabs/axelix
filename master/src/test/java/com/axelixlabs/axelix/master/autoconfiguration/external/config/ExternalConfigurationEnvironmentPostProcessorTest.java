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
package com.axelixlabs.axelix.master.autoconfiguration.external.config;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.external.config.ExternalConfigurationEnvironmentPostProcessor.EXTERNAL_CONFIG_OPTION;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.ExternalConfigurationEnvironmentPostProcessor.SPRING_CLOUD_CONFIG_ENABLED_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.ExternalConfigurationEnvironmentPostProcessor.SPRING_CONFIG_IMPORT_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Yanin
 */
class ExternalConfigurationEnvironmentPostProcessorTest {

    private final ExternalConfigurationEnvironmentPostProcessor postProcessor =
            new ExternalConfigurationEnvironmentPostProcessor();

    private final SpringApplication application = new SpringApplication();

    @Test
    void shouldRunBeforeConfigDataEnvironmentPostProcessor() {
        assertThat(postProcessor.getOrder())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE)
                .isLessThan(ConfigDataEnvironmentPostProcessor.ORDER);
    }

    @Test
    void shouldDoNothingWhenPropertyIsAbsent() {
        var environment = new MockEnvironment();

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY)).isNull();
        assertThat(environment.getProperty(SPRING_CLOUD_CONFIG_ENABLED_PROPERTY))
                .isNull();
    }

    @Test
    void shouldInjectConfigServerImportWhenOptionIsSpringCloudConfig() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION, "spring-cloud-config");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY)).isEqualTo("configserver:");
    }

    @Test
    void shouldEnableCloudConfigExplicitlyWhenOptionIsSpringCloudConfig() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION, "spring-cloud-config");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_CLOUD_CONFIG_ENABLED_PROPERTY))
                .isEqualTo("true");
    }

    @Test
    void shouldInjectPropertiesCaseInsensitively() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION, "SPRING-CLOUD-CONFIG");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY)).isEqualTo("configserver:");
        assertThat(environment.getProperty(SPRING_CLOUD_CONFIG_ENABLED_PROPERTY))
                .isEqualTo("true");
    }
}
