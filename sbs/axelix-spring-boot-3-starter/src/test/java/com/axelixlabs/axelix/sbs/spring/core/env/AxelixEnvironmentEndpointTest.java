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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Artemiy Degtyarev
 */
class AxelixEnvironmentEndpointTest extends AbstractEnvSharedContextTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private ConfigurableEnvironment environment;

    @BeforeEach
    void before() {
        environment.getSystemProperties().put("axelix.env.test.prop1", "systemValue");
        environment.getSystemProperties().put("axelix.env.test.prop2", "systemValue");
        environment.getSystemProperties().put("axelix.env.test.prop3", "systemValue");
        environment.getSystemProperties().put("AXELIX_FOR_SANITIZATION", "shouldBeSanitized");
    }

    @ParameterizedTest(name = "Property ''{0}'' should resolve from highest-precedence source")
    @MethodSource("propertyExpectations")
    void shouldSelectPrimaryPropertyFromHighestPrecedenceSource(String propertyName, String expectedValue) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.asViewer().getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        var propertyAppearances = findPropertyAppearances(propertyName, response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(propertyAppearances)
                .isNotEmpty()
                .filteredOn(e -> e.getValue().isPrimary())
                .hasSize(1)
                .first()
                .extracting(e -> e.getValue().getValue())
                .isEqualTo(expectedValue);
    }

    @ParameterizedTest(name = "Property ''{0}'' should have value ''{1}'' for ''{2}''")
    @MethodSource("sanitizationArgsSource")
    void shouldReturnACorrectValueForTheGivenPropertyConsideringTheRole(String propertyName, String value, Role role) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.withRole(role).getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        var propertyAppearances = findPropertyAppearances(propertyName, response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(propertyAppearances)
                .filteredOn(it -> Objects.equals(it.getValue().getName(), propertyName))
                .first()
                .extracting(it -> it.getValue().getValue())
                .isEqualTo(value);
    }

    @Test
    void shouldReturnValidJsonStructure() {
        ResponseEntity<String> response = restTemplate.asViewer().getForEntity("/actuator/axelix-env", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String responseBody = response.getBody();

        // We're not exactly sure about the shape of the returned json. It may and it would
        // vary depending on the CI/CD runner, on the overall environment and spring version etc.
        // So we just check the basic invariants.
        assertThat(responseBody).isNotNull();

        assertThatJson(responseBody).node("activeProfiles").isNotNull().isArray();

        assertThatJson(responseBody).node("defaultProfiles").isNotNull().isArray();

        assertThatJson(responseBody)
                .inPath("propertySources[*].properties")
                .isArray()
                .allSatisfy(properties -> assertThatJson(properties).isArray().allSatisfy(property -> {
                    assertThatJson(property)
                            .isObject()
                            .containsKey("name")
                            .containsKey("isPrimary")
                            .containsKey("value")
                            .containsKey("configPropsBeanName")
                            .containsKey("description")
                            .containsKey("injectionPoints");

                    assertThatJson(property).node("isPrimary").isBoolean();

                    assertThatJson(property)
                            .inPath("injectionPoints[*]")
                            .isArray()
                            .allSatisfy(injectionPoint -> {
                                assertThatJson(injectionPoint)
                                        .isObject()
                                        .containsKeys("beanName", "injectionType", "targetName", "propertyExpression");
                            });
                }));
    }

    @ParameterizedTest
    @MethodSource("propertyName")
    void shouldReturnTheBeanNameThatMatchesTheConfigProps(String propertyName) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.asEditor().getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        var propertyAppearances = findPropertyAppearances(propertyName, response);

        assertThat(propertyAppearances)
                .extracting(e -> e.getValue().getConfigPropsBeanName())
                .containsOnly(EnvSharedTestConfig.AxelixConfigurationProperties.class.getName());
    }

    @ParameterizedTest
    @MethodSource("propertySourceDescription")
    void shouldReturnDescriptionKnownPropertySource(String sourceName, String sourceDescription) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.asViewer().getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        assertThat(response.getBody().getPropertySources())
                .filteredOn(e -> e.getName().equals(sourceName))
                .first()
                .satisfies(e -> e.getDescription().equals(sourceDescription));
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-env")
    void negativeAuthTests() {}

    private static Stream<Arguments> propertyExpectations() {
        return Stream.of(
                Arguments.of("axelix.env.test.prop1", "fromTestSource"),
                Arguments.of("axelix.env.test.prop2", "dynamicValue"),
                Arguments.of("axelix.env.test.prop3", "fromCommandLine"));
    }

    public static Stream<Arguments> sanitizationArgsSource() {
        return Stream.of(
                Arguments.of("axelix.env.test.toBeSanitized", "******", DefaultRole.EDITOR),
                Arguments.of("AXELIX_FOR_SANITIZATION", "******", DefaultRole.EDITOR),
                Arguments.of("axelix.env.test.toBeSanitized", "******", DefaultRole.VIEWER),
                Arguments.of("AXELIX_FOR_SANITIZATION", "******", DefaultRole.VIEWER),
                Arguments.of("axelix.env.test.toBeSanitized", "shouldBeSanitized", DefaultRole.ADMIN),
                Arguments.of("AXELIX_FOR_SANITIZATION", "shouldBeSanitized", DefaultRole.ADMIN));
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("axelix.prop.test.tags.environment"),
                Arguments.of("axelix.prop.test.tags.version"),
                Arguments.of("axelix.prop.test.enabled-contexts"),
                Arguments.of("axelix.prop.test.http-client.requests[0].name"),
                Arguments.of("axelix.prop.test.http-client.requests[0].base-url"),
                Arguments.of("axelix.prop.test.http-client.requests[0].methods[0].type"),
                Arguments.of("axelix.prop.test.http-client.requests[0].methods[0].retries[0].count"),
                Arguments.of("axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout"),
                Arguments.of("axelix.prop.test.http-client.requests[0].methods[1].type"),
                Arguments.of("axelix.prop.test.http-client.requests[1].name"),
                Arguments.of("axelix.prop.test.http-client.requests[1].base-url"),
                Arguments.of("axelix.prop.test.http-client.requests[1].methods[0].type"),
                Arguments.of("axelix.prop.test.http-client.requests[1].methods[0].retries[0].count"),
                Arguments.of("axelix.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level"));
    }

    private static Stream<Arguments> propertySourceDescription() {
        return Stream.of(
                Arguments.of(
                        StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                        "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'"),
                Arguments.of(
                        "server.ports",
                        "Contains the 'server.port' property from 'application.*', which defines the web server port (8080 by default)."));
    }

    private static List<Map.Entry<String, EnvironmentFeed.Property>> findPropertyAppearances(
            String propertyName, ResponseEntity<EnvironmentFeed> response) {

        return response.getBody().getPropertySources().stream()
                .flatMap(src -> src.getProperties().stream()
                        .filter(p -> p.getName().equals(propertyName))
                        .map(p -> Map.entry(src.getName(), p)))
                .toList();
    }
}
