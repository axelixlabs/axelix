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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.core.env.StandardEnvironment;

import com.axelixlabs.axelix.sbs.spring.core.env.PropertySourceDescription.PropertySourceDisplayData;

import static com.axelixlabs.axelix.sbs.spring.core.env.PropertySourceDescription.resolveDisplayData;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PropertySourceDescription}
 *
 * @since 11.02.2026
 * @author Nikita Kirillov
 */
class PropertySourceDescriptionTest {

    @ParameterizedTest
    @MethodSource("configResourceSources")
    void shouldParseConfigResourceSourceCorrectly(
            String sourceName, String expectedDisplayName, String expectedDescription) {

        PropertySourceDisplayData result = resolveDisplayData(sourceName);

        assertThat(result.displayName()).isEqualTo(expectedDisplayName);
        assertThat(result.description()).isEqualTo(expectedDescription);
    }

    private static Stream<Arguments> configResourceSources() {
        return Stream.of(
                Arguments.of(
                        "Config resource 'class path resource [application.properties]' via location 'optional:classpath:/'",
                        "application.properties",
                        "Properties that are loaded from application.properties located in optional:classpath:/"),
                Arguments.of(
                        "Config resource 'class path resource [application-dev.properties]' via location 'optional:classpath:/'",
                        "application-dev.properties",
                        "Properties that are loaded from application-dev.properties located in optional:classpath:/"),
                Arguments.of(
                        "Config resource 'class path resource [config/application-local.yaml]' via location 'optional:classpath:/config/'",
                        "application-local.yaml",
                        "Properties that are loaded from application-local.yaml located in optional:classpath:/config/"),
                Arguments.of(
                        "Config resource 'file [/etc/app/application-prod.yaml]' via location 'optional:file:/etc/app/'",
                        "application-prod.yaml",
                        "Properties that are loaded from application-prod.yaml located in optional:file:/etc/app/"));
    }

    @ParameterizedTest
    @MethodSource("unknownSources")
    void shouldReturnOriginalNameAndNullDescriptionForUnknownSources(String sourceName, String expectedDescription) {
        PropertySourceDisplayData result = resolveDisplayData(sourceName);

        assertThat(result.displayName()).isEqualTo(sourceName);
        assertThat(result.description()).isEqualTo(expectedDescription);
    }

    private static Stream<Arguments> unknownSources() {
        return Stream.of(
                Arguments.of("someUnknownSource", null),
                // Starts with "Config resource", but not match
                Arguments.of(
                        "Config resource 'unexpected format'",
                        "Contains properties from the 'application*.properties/yaml' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources."));
    }

    @ParameterizedTest
    @MethodSource("knownNonConfigResourceSources")
    void shouldReturnOriginalNameAndDescriptionForKnownSources(String sourceName, String expectedDescription) {

        PropertySourceDisplayData result = resolveDisplayData(sourceName);

        assertThat(result.displayName()).isEqualTo(sourceName);
        assertThat(result.description()).isEqualTo(expectedDescription);
    }

    private static Stream<Arguments> knownNonConfigResourceSources() {
        return Stream.of(
                Arguments.of(
                        StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                        PropertySourceDescription.SYSTEM_PROPERTIES.getDescription()),
                Arguments.of(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        PropertySourceDescription.SYSTEM_ENVIRONMENT.getDescription()));
    }
}
