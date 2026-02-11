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

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.Property;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.PropertySource;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultEnvPropertyEnricher}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(args = "--fooBar=fromArgs")
@EnableConfigurationProperties(EndpointsConfigurationProperties.class)
@Import({EnvironmentTestConfig.class, DefaultEnvPropertyEnricherTest.CurrentTestConfig.class})
class DefaultEnvPropertyEnricherTest {

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private EnvPropertyEnricher enricher;

    @TestConfiguration
    static class CurrentTestConfig {

        @Bean
        SmartSanitizingFunction smartSanitizingFunction() {
            return new SmartSanitizingFunction(List.of(), new DefaultPropertyNameNormalizer());
        }
    }

    @BeforeAll
    static void beforeAll() {
        System.setProperty("foo.bar", "system.property");
    }

    @Test
    void shouldEnrichAllPropertiesWithPrimaryField() {
        EnvironmentDescriptor defaultDescriptor = environmentEndpoint.environment(null);

        EnvironmentFeed environmentFeed = enricher.enrich(defaultDescriptor);

        assertThat(environmentFeed).isNotNull();
        assertThat(environmentFeed.getActiveProfiles()).isNotNull();
        assertThat(environmentFeed.getDefaultProfiles()).isNotNull();
        assertThat(environmentFeed.getPropertySources()).isNotEmpty();

        // property from the command line args should win
        // https://docs.spring.io/spring-boot/reference/features/external-config.html
        assertThat(findProperty(environmentFeed, StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, "foo.bar")
                        .isPrimary())
                .isFalse();

        assertThat(findProperty(environmentFeed, "commandLineArgs", "fooBar").isPrimary())
                .isTrue();
    }

    @Test
    void shouldNormalizeConfigResourcePropertySourceNameAndDescription() {
        EnvironmentDescriptor defaultDescriptor = environmentEndpoint.environment(null);

        EnvironmentFeed environmentFeed = enricher.enrich(defaultDescriptor);

        List<PropertySource> configResourceSources = environmentFeed.getPropertySources().stream()
                .filter(it -> it.getSourceName().endsWith(".yaml"))
                .toList();

        assertThat(configResourceSources).isNotEmpty();

        configResourceSources.forEach(source -> {
            assertThat(source.getSourceName()).isEqualTo("application.yaml");

            assertThat(source.getSourceDescription())
                    .isEqualTo("Properties that are loaded from application.yaml located in optional:classpath:/");
        });
    }

    private static Property findProperty(
            EnvironmentFeed environmentFeed, String propertySourceName, String propertyName) {
        PropertySource propertySource = environmentFeed.getPropertySources().stream()
                .filter(it -> it.getSourceName().equals(propertySourceName))
                .findFirst()
                .orElseThrow();

        return propertySource.getProperties().stream()
                .filter(it -> it.getPropertyName().equals(propertyName))
                .findFirst()
                .orElseThrow();
    }
}
