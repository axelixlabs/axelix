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

import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.AXELIX_PREFIX;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.EXTERNAL_CONFIG_OPTION;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.SPRING_CLOUD_CONFIG_ENABLED_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.SPRING_CONFIG_IMPORT_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.external.config.AxelixConfigConstant.SPRING_PREFIX;
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

        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY.value()))
                .isNull();
        assertThat(environment.getProperty(SPRING_CLOUD_CONFIG_ENABLED_PROPERTY.value()))
                .isNull();
    }

    @Test
    void shouldInjectConfigServerImportWhenOptionIsSpringCloudConfig() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION.value(), "spring-cloud-config");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY.value()))
                .isEqualTo("configserver:");
    }

    @Test
    void shouldEnableCloudConfigExplicitlyWhenOptionIsSpringCloudConfig() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION.value(), "spring-cloud-config");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_CLOUD_CONFIG_ENABLED_PROPERTY.value()))
                .isEqualTo("true");
    }

    @Test
    void shouldInjectPropertiesCaseInsensitively() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION.value(), "SPRING-CLOUD-CONFIG");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY.value()))
                .isEqualTo("configserver:");
        assertThat(environment.getProperty(SPRING_CLOUD_CONFIG_ENABLED_PROPERTY.value()))
                .isEqualTo("true");
    }

    @Test
    void shouldMapAllFivePropertiesWhenPresent() {
        var environment = createEnvironment();

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_PREFIX.value() + "uri"))
                .isEqualTo("http://custom-config-server:8888");
        assertThat(environment.getProperty(SPRING_PREFIX.value() + "label")).isEqualTo("master-branch");
        assertThat(environment.getProperty(SPRING_PREFIX.value() + "name")).isEqualTo("axelix-application");
        assertThat(environment.getProperty(SPRING_PREFIX.value() + "username")).isEqualTo("axelix-user");
        assertThat(environment.getProperty(SPRING_PREFIX.value() + "password")).isEqualTo("secure-password");
    }

    private MockEnvironment createEnvironment() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION.value(), "spring-cloud-config");

        environment.setProperty(AXELIX_PREFIX.value() + "uri", "http://custom-config-server:8888");
        environment.setProperty(AXELIX_PREFIX.value() + "label", "master-branch");
        environment.setProperty(AXELIX_PREFIX.value() + "name", "axelix-application");
        environment.setProperty(AXELIX_PREFIX.value() + "username", "axelix-user");
        environment.setProperty(AXELIX_PREFIX.value() + "password", "secure-password");
        return environment;
    }

    @Test
    void shouldNotMapNullOrBlankProperties() {
        var environment = new MockEnvironment();
        environment.setProperty(EXTERNAL_CONFIG_OPTION.value(), "spring-cloud-config");

        environment.setProperty(AXELIX_PREFIX.value() + "uri", "");
        environment.setProperty(AXELIX_PREFIX.value() + "label", "   ");

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(environment.getProperty(SPRING_PREFIX.value() + "uri")).isNull();
        assertThat(environment.getProperty(SPRING_PREFIX.value() + "label")).isNull();
    }
}
