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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.LazyInitializationBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EndpointPathMappingValidationAutoConfiguration}.
 *
 * @author Andrey Kotov
 */
class EndpointPathMappingValidationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EndpointPathMappingValidationAutoConfiguration.class));

    @Test // GH-1803
    void shouldStartWithoutPathMappings() {
        // given / when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
        });
    }

    @ParameterizedTest // GH-1803
    @ValueSource(
            strings = {
                "management.endpoints.web.path-mapping.axelix-beans=custom-beans",
                "management.endpoints.web.path-mapping.axelix-beans=axelix-beans",
                "management.endpoints.web.path-mapping.axelix-beans=",
                "management.endpoints.web.path-mapping.axelix-thread-dump=custom-threads"
            })
    void shouldRejectAxelixPathMappings(String mapping) {
        // given.
        ApplicationContextRunner runner = contextRunner.withPropertyValues(mapping);

        // when.
        runner.run(context -> {
            // then.
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class);
        });
    }

    @ParameterizedTest // GH-1803
    @ValueSource(strings = {"axelixbeans", "aXeLiXBeans", "axe.lix.beans", "axe-lix-beans"})
    void shouldRejectEquivalentAxelixEndpointIds(String endpointId) {
        // given.
        ApplicationContextRunner runner = contextRunner.withPropertyValues(
                "management.endpoints.web.path-mapping." + endpointId + "=custom-beans");

        // when.
        runner.run(context -> {
            // then.
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class);
        });
    }

    @ParameterizedTest // GH-1803
    @ValueSource(
            strings = {
                "management.endpoints.web.base-path=/manage",
                "management.endpoints.web.path-mapping.health=healthcheck",
                "management.endpoints.web.path-mapping.info=axelix-info",
                "management.endpoints.web.path-mapping.custom-axelix-beans=custom-beans",
                "management.endpoints.web.path-mapping.axelix-unknown=custom-endpoint"
            })
    void shouldAllowSupportedEndpointConfiguration(String property) {
        // given.
        ApplicationContextRunner runner = contextRunner.withPropertyValues(property);

        // when.
        runner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
        });
    }

    @ParameterizedTest // GH-1803
    @ValueSource(
            strings = {
                "management.endpoint.axelix-beans.enabled=false",
                "management.endpoints.web.exposure.exclude=axelix-beans",
                "management.endpoints.web.exposure.include=*"
            })
    void shouldRejectMappingRegardlessOfEndpointAvailability(String property) {
        // given.
        ApplicationContextRunner runner = contextRunner.withPropertyValues(
                property, "management.endpoints.web.path-mapping.axelix-beans=custom-beans");

        // when.
        runner.run(context -> {
            // then.
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class);
        });
    }

    @Test // GH-1803
    void shouldRejectMappingWhenLazyInitializationEnabled() {
        // given.
        ApplicationContextRunner runner = contextRunner
                .withInitializer(context ->
                        context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor()))
                .withPropertyValues("management.endpoints.web.path-mapping.axelix-beans=custom-beans");

        // when.
        runner.run(context -> {
            // then.
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class);
        });
    }

    @Test // GH-1803
    void shouldSkipValidationWhenStarterDisabled() {
        // given.
        ApplicationContextRunner runner = contextRunner.withPropertyValues(
                "axelix.sbs.enabled=false", "management.endpoints.web.path-mapping.axelix-beans=custom-beans");

        // when.
        runner.run(context -> {
            // then.
            assertThat(context).hasNotFailed();
        });
    }
}
