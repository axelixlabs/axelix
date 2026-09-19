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
package com.axelixlabs.axelix.sbs.spring.core.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixBeansEndpointAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * End-to-end test for the {@code axelix.sbs.enabled} kill-switch.
 *
 * @author Nikita Kirillov
 */
class AxelixStarterEnabledSwitchTest {

    @SpringBootApplication
    static class TestApplication {}

    private static final String IMPORTS_RESOURCE =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void everyStarterAutoConfigurationIsAbsent_whenAxelixSbsEnabledIsFalse() throws IOException {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestApplication.class)
                .web(WebApplicationType.NONE)
                .properties("axelix.sbs.enabled=false")
                .run()) {

            for (String className : readAutoConfigurationClassNames()) {
                assertThat(context.containsBeanDefinition(className))
                        .as("%s should not be applied when axelix.sbs.enabled=false", className)
                        .isFalse();
            }
        }
    }

    @Test
    void everyAutoConfigurationIsUpByDefault() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {

            assertThatCode(() -> context.getBean(AxelixBeansEndpointAutoConfiguration.class))
                    .doesNotThrowAnyException();
        }
    }

    private List<String> readAutoConfigurationClassNames() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(IMPORTS_RESOURCE)) {
            assertThat(stream)
                    .as("%s must exist on the test classpath", IMPORTS_RESOURCE)
                    .isNotNull();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .collect(Collectors.toList());
            }
        }
    }
}
