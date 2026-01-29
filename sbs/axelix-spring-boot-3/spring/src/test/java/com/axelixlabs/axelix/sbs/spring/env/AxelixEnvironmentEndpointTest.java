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
package com.axelixlabs.axelix.sbs.spring.env;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;
import com.axelixlabs.axelix.sbs.spring.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.configprops.SmartSanitizingFunction;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--axelix.env.test.prop3=fromCommandLine"},
        properties = {
            "axelix.env.test.prop2=systemValue2",
            "management.endpoint.env.show-values=always",
        })
@TestPropertySource(
        properties = {
            // properties -> shouldSelectPrimaryPropertyFromHighestPrecedenceSource
            "axelix.env.test.prop1=fromTestSource",
            "axelix.env.test.toBeSanitized=shouldBeSanitized",

            // properties -> shouldReturnTheBeanNameThatMatchesTheConfigProps
            "axelix.prop.test.tags.environment=test",
            "axelix.prop.test.tags.version=1.0.0",
            "axelix.prop.test.enabled-contexts=user-service,payment-service",
            "axelix.prop.test.http-client.requests[0].name=user-api",
            "axelix.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axelix.prop.test.http-client.requests[0].methods[0].type=GET",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axelix.prop.test.http-client.requests[0].methods[1].type=POST",
            "axelix.prop.test.http-client.requests[1].name=payment-api",
            "axelix.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axelix.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
@EnableConfigurationProperties({
    AxelixEnvironmentEndpointTest.AxelixPropTest.class,
    EndpointsConfigurationProperties.class
})
@Import({EnvironmentTestConfig.class})
class AxelixEnvironmentEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfigurableEnvironment environment;

    @BeforeEach
    void before() {
        environment.getSystemProperties().put("axelix.env.test.prop1", "systemValue");
        environment.getSystemProperties().put("axelix.env.test.prop2", "systemValue");
        environment.getSystemProperties().put("axelix.env.test.prop3", "systemValue");
        environment.getSystemProperties().put("AXELIX_FOR_SANITIZATION", "shouldBeSanitized");
    }

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axelix.env.test.prop2", () -> "dynamicValue");
    }

    @ParameterizedTest(name = "Property ''{0}'' should resolve from highest-precedence source")
    @MethodSource("propertyExpectations")
    void shouldSelectPrimaryPropertyFromHighestPrecedenceSource(String propertyName, String expectedValue) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        var propertyAppearances = findPropertyAppearances(propertyName, response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(propertyAppearances)
                .isNotEmpty()
                .filteredOn(e -> e.getValue().isPrimary())
                .hasSize(1)
                .first()
                .extracting(e -> e.getValue().value())
                .isEqualTo(expectedValue);
    }

    private static Stream<Arguments> propertyExpectations() {
        return Stream.of(
                Arguments.of("axelix.env.test.prop1", "fromTestSource"),
                Arguments.of("axelix.env.test.prop2", "dynamicValue"),
                Arguments.of("axelix.env.test.prop3", "fromCommandLine"));
    }

    @ParameterizedTest(name = "Property ''{0}'' should have sanitized value")
    @MethodSource("sanitizationArgsSource")
    void shouldSanitizeAllAppearancesOfTheGivenProperty(String propertyName) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        var propertyAppearances = findPropertyAppearances(propertyName, response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(propertyAppearances).extracting(it -> it.getValue().value()).containsOnly("******");
    }

    public static Stream<Arguments> sanitizationArgsSource() {
        return Stream.of(Arguments.of("axelix.env.test.toBeSanitized"), Arguments.of("AXELIX_FOR_SANITIZATION"));
    }

    @Test
    void shouldReturnValidJsonStructure() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axelix-env", String.class);

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
                            .containsKey("propertyName")
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
                restTemplate.getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        var propertyAppearances = findPropertyAppearances(propertyName, response);

        assertThat(propertyAppearances)
                .extracting(e -> e.getValue().configPropsBeanName())
                .containsOnly(AxelixPropTest.class.getName());
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

    @ParameterizedTest
    @MethodSource("propertySourceDescription")
    void shouldReturnDescriptionKnownPropertySource(String sourceName, String sourceDescription) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        assertThat(response.getBody().propertySources())
                .filteredOn(e -> e.sourceName().equals(sourceName))
                .first()
                .satisfies(e -> e.sourceDescription().equals(sourceDescription));
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

        return response.getBody().propertySources().stream()
                .flatMap(src -> src.properties().stream()
                        .filter(p -> p.propertyName().equals(propertyName))
                        .map(p -> Map.entry(src.sourceName(), p)))
                .toList();
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public record AxelixPropTest(Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class AxelixEnvironmentEndpointTestConfiguration {

        @Bean
        public AxelixEnvironmentEndpoint axelixEnvironmentEndpoint(
                Environment environment,
                SmartSanitizingFunction smartSanitizingFunction,
                EnvPropertyEnricher envPropertyEnricher) {
            return new AxelixEnvironmentEndpoint(environment, smartSanitizingFunction, envPropertyEnricher);
        }

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(
                    List.of("axelix.env.test.toBeSanitized", "AXELIX_FOR_SANITIZATION"), propertyNameNormalizer);
        }
    }
}
