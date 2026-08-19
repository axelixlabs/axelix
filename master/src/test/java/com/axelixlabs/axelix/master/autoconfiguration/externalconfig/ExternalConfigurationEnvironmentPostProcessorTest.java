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
package com.axelixlabs.axelix.master.autoconfiguration.externalconfig;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static com.axelixlabs.axelix.master.autoconfiguration.externalconfig.ExternalConfigurationEnvironmentPostProcessor.AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS;
import static com.axelixlabs.axelix.master.autoconfiguration.externalconfig.ExternalConfigurationEnvironmentPostProcessor.SPRING_CONFIG_IMPORT_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ExternalConfigurationEnvironmentPostProcessor}.
 *
 * @author Ilya Naumov
 */
class ExternalConfigurationEnvironmentPostProcessorTest {
    private final ExternalConfigurationEnvironmentPostProcessor postProcessor =
            new ExternalConfigurationEnvironmentPostProcessor();

    private final SpringApplication application = new SpringApplication();

    @Test // GH-1489
    void shouldRunBeforeConfigDataEnvironmentPostProcessor() {
        // when.
        assertThat(postProcessor.getOrder())
                // then.
                .isLessThan(ConfigDataEnvironmentPostProcessor.ORDER);
    }

    @Test // GH-1489
    void shouldDoNothingWhenPropertyIsAbsent() {
        // given.
        MockEnvironment environment = new MockEnvironment();

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY)).isNull();

        for (ExternalConfigOption configType : ExternalConfigOption.values()) {
            for (String key : configType.getProperties().keySet()) {
                assertThat(environment.getProperty(key)).isNull();
            }
        }
    }

    @ParameterizedTest // GH-1489
    @ValueSource(strings = {"INVALID_OPTION", "VAULT VAULT"})
    void shouldThrowWhenInvalidOptionsAreProvided(String invalidOption) {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS, invalidOption);

        // when.
        assertThatThrownBy(() -> postProcessor.postProcessEnvironment(environment, application))
                // then.
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(invalidOption);
    }

    @Test // GH-1489
    void shouldProperlyHandleDuplicateOptions() {
        // given.
        MockEnvironment environment = new MockEnvironment();
        ExternalConfigOption duplicateOption = ExternalConfigOption.VAULT;
        environment.setProperty(
                AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS,
                String.join(",", List.of(duplicateOption.toString(), duplicateOption.toString())));

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY))
                .isEqualTo(duplicateOption.getImportLocation());

        for (String key : duplicateOption.getProperties().keySet()) {
            assertThat(environment.getProperty(key)).isNotNull();
        }
    }

    @ParameterizedTest // GH-1489
    @EnumSource(ExternalConfigOption.class)
    void shouldHandleSingleOption(ExternalConfigOption option) {
        // given.
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS, option.toString());

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        assertThat(environment.getProperty(SPRING_CONFIG_IMPORT_PROPERTY)).isEqualTo(option.getImportLocation());

        for (String key : option.getProperties().keySet()) {
            assertThat(environment.getProperty(key)).isNotNull();
        }
    }

    @Test // GH-1489
    void shouldProperlyHandleAllOptionsInOrder() {
        // given.
        MockEnvironment environment = new MockEnvironment();

        List<String> reversedOptions = Stream.of(ExternalConfigOption.values())
                .sorted(Comparator.comparing(ExternalConfigOption::getOrder).reversed())
                .map(ExternalConfigOption::toString)
                .toList();

        environment.setProperty(AXELIX_MASTER_EXTERNAL_CONFIG_OPTIONS, String.join(",", reversedOptions));

        // when.
        postProcessor.postProcessEnvironment(environment, application);

        // then.
        List<String> orderedImportLocations = Stream.of(ExternalConfigOption.values())
                .sorted(Comparator.comparing(ExternalConfigOption::getOrder))
                .map(ExternalConfigOption::getImportLocation)
                .toList();

        List<String> environmentImportLocations = Binder.get(environment)
                .bind(SPRING_CONFIG_IMPORT_PROPERTY, Bindable.listOf(String.class))
                .orElseGet(List::of);

        assertThat(environmentImportLocations).containsExactlyElementsOf(orderedImportLocations);

        for (ExternalConfigOption configType : ExternalConfigOption.values()) {
            for (String key : configType.getProperties().keySet()) {
                assertThat(environment.getProperty(key)).isNotNull();
            }
        }
    }
}
