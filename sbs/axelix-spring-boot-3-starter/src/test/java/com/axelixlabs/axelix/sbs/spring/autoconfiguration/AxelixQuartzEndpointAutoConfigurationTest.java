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

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.quartz.AxelixQuartzEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixQuartzEndpointAutoConfiguration}.
 *
 * @author Nikita Kirillov
 */
class AxelixQuartzEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "management.endpoints.web.exposure.include=axelix-quartz",
                    "spring.quartz.auto-startup=false",
                    "spring.quartz.properties.org.quartz.threadPool.threadCount=1")
            .withConfiguration(AutoConfigurations.of(AxelixQuartzEndpointAutoConfiguration.class));

    @Test
    void shouldCreateQuartzEndpointWhenSchedulerExists() {
        // given / when.
        contextRunner
                .withConfiguration(AutoConfigurations.of(QuartzAutoConfiguration.class))
                .run(context -> {
                    // then.
                    assertThat(context).hasSingleBean(AxelixQuartzEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutScheduler() {
        // given / when.
        contextRunner.run(context -> {
            // then.
            assertThat(context).doesNotHaveBean(AxelixQuartzEndpoint.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutQuartz() {
        // given / when.
        contextRunner.withClassLoader(new FilteredClassLoader("org.quartz")).run(context -> {
            // then.
            assertThat(context).hasNotFailed().doesNotHaveBean(AxelixQuartzEndpoint.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        // given / when.
        contextRunner
                .withConfiguration(AutoConfigurations.of(QuartzAutoConfiguration.class))
                .withPropertyValues("management.endpoint.axelix-quartz.enabled=false")
                .run(context -> {
                    // then.
                    assertThat(context).doesNotHaveBean(AxelixQuartzEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenStarterDisabled() {
        // given / when.
        contextRunner
                .withConfiguration(AutoConfigurations.of(QuartzAutoConfiguration.class))
                .withPropertyValues("axelix.sbs.enabled=false")
                .run(context -> {
                    // then.
                    assertThat(context).doesNotHaveBean(AxelixQuartzEndpoint.class);
                });
    }
}
