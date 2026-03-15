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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PropertySourceDescriptionResolver}
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
class DefaultPropertySourceDescriptionResolverTest {
    public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";
    public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";

    PropertySourceDescriptionResolver propertySourceDescriptionResolver =
            new DefaultPropertySourceDescriptionResolver();

    @ParameterizedTest
    @MethodSource("configResourceSources")
    void shouldParseConfigResourceSourceCorrectly(
            String sourceName, String expectedDisplayName, String expectedDescription) {

        PropertySourceDisplayData result = propertySourceDescriptionResolver.resolveDisplayData(
                sourceName, TestPropertySourceDescription.values());

        assertThat(result.getDisplayName()).isEqualTo(expectedDisplayName);
        assertThat(result.getDescription()).isEqualTo(expectedDescription);
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
        PropertySourceDisplayData result = propertySourceDescriptionResolver.resolveDisplayData(
                sourceName, TestPropertySourceDescription.values());

        assertThat(result.getDisplayName()).isEqualTo(sourceName);
        assertThat(result.getDescription()).isEqualTo(expectedDescription);
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

        PropertySourceDisplayData result = propertySourceDescriptionResolver.resolveDisplayData(
                sourceName, TestPropertySourceDescription.values());

        assertThat(result.getDisplayName()).isEqualTo(sourceName);
        assertThat(result.getDescription()).isEqualTo(expectedDescription);
    }

    private static Stream<Arguments> knownNonConfigResourceSources() {
        return Stream.of(
                Arguments.of(
                        SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                        TestPropertySourceDescription.SYSTEM_PROPERTIES.getDescription()),
                Arguments.of(
                        SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        TestPropertySourceDescription.SYSTEM_ENVIRONMENT.getDescription()));
    }

    enum TestPropertySourceDescription implements PropertySourceDescription {
        SYSTEM_PROPERTIES(
                SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                PropertySourceCustomDescription.SYSTEM_PROPERTIES.getDescription()),
        SYSTEM_ENVIRONMENT(
                SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                PropertySourceCustomDescription.SYSTEM_ENVIRONMENT.getDescription()),
        APPLICATION_PROPERTIES(
                "Config resource", PropertySourceCustomDescription.APPLICATION_PROPERTIES.getDescription());

        private final String sourceName;
        private final String description;

        TestPropertySourceDescription(String sourceName, String description) {
            this.sourceName = sourceName;
            this.description = description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getSourceName() {
            return sourceName;
        }
    }
}
