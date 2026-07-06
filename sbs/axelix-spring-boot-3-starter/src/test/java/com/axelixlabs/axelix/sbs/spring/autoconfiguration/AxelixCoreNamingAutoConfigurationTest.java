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
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.beans.AxelixBeanRenamingProcessor;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.heapdump.AxelixHeapDumpEndpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Integration test for {@link AxelixCoreNamingAutoConfiguration}
 *
 * @author Vycheslav Yanin
 * @since 05.07.2026
 */
class AxelixCoreNamingAutoConfigurationTest {

    private static final String PREFIX = "axelix";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AxelixCoreNamingAutoConfiguration.class));

    @Test
    void shouldRenameBeanToAxelixConvention() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(EndpointsConfigurationPropertiesAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(AxelixBeanRenamingProcessor.class);

                    String[] beanNames = context.getBeanNamesForType(EndpointsConfigurationProperties.class);
                    assertThat(beanNames).isNotEmpty();
                    String actualBeanName = beanNames[0];
                    assertThat(actualBeanName).startsWith(PREFIX);
                    assertEquals("axelixEndpointsConfigurationProperties", actualBeanName);
                });
    }

    @Test
    void shouldNotRenameBeanToAxelixConvention_WhenBeanAlreadyCompliesWithConvention() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(AxelixHeapDumpEndpointAutoConfiguration.class))
                .withPropertyValues(
                        "management.endpoint.axelixheapdump.enabled=true",
                        "management.endpoints.web.exposure.include=axelixheapdump")
                .run(context -> {
                    assertThat(context).hasSingleBean(AxelixBeanRenamingProcessor.class);
                    assertThat(context).hasSingleBean(AxelixHeapDumpEndpoint.class);

                    String[] beanNames = context.getBeanNamesForType(AxelixHeapDumpEndpoint.class);
                    assertThat(beanNames).isNotEmpty();
                    String actualBeanName = beanNames[0];
                    assertFalse(actualBeanName.startsWith(PREFIX + PREFIX));
                    assertEquals("axelixHeapDumpEndpoint", actualBeanName);
                });
    }
}
