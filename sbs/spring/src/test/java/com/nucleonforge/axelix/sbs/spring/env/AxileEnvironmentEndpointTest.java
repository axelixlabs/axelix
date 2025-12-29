/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.env;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axelix.common.api.env.EnvironmentFeed;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxileEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--axile.env.test.prop3=fromCommandLine"},
        properties = {
            "axile.env.test.prop2=systemValue2",
            "management.endpoint.env.show-values=always",
        })
@TestPropertySource(
        properties = {
            // properties -> shouldSelectPrimaryPropertyFromHighestPrecedenceSource
            "axile.env.test.prop1=fromTestSource",

            // properties -> shouldReturnTheBeanNameThatMatchesTheConfigProps
            "axile.prop.test.tags.environment=test",
            "axile.prop.test.tags.version=1.0.0",
            "axile.prop.test.enabled-contexts=user-service,payment-service",
            "axile.prop.test.http-client.requests[0].name=user-api",
            "axile.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axile.prop.test.http-client.requests[0].methods[0].type=GET",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axile.prop.test.http-client.requests[0].methods[1].type=POST",
            "axile.prop.test.http-client.requests[1].name=payment-api",
            "axile.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axile.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axile.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axile.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
@EnableConfigurationProperties(AxileEnvironmentEndpointTest.AxilePropTest.class)
@Import({EnvironmentTestConfig.class})
class AxileEnvironmentEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfigurableEnvironment environment;

    @BeforeEach
    void before() {
        environment.getSystemProperties().put("axile.env.test.prop1", "systemValue");
        environment.getSystemProperties().put("axile.env.test.prop2", "systemValue");
        environment.getSystemProperties().put("axile.env.test.prop3", "systemValue");
    }

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axile.env.test.prop2", () -> "dynamicValue");
    }

    @ParameterizedTest(name = "Property ''{0}'' should resolve from highest-precedence source")
    @MethodSource("propertyExpectations")
    void shouldSelectPrimaryPropertyFromHighestPrecedenceSource(String propertyName, String expectedValue) {
        ResponseEntity<EnvironmentFeed> response =
                restTemplate.getForEntity("/actuator/axelix-env", EnvironmentFeed.class);

        var propertyAppearances = response.getBody().propertySources().stream()
                .flatMap(src -> src.properties().stream()
                        .filter(p -> p.propertyName().equals(propertyName))
                        .map(p -> Map.entry(src.sourceName(), p)))
                .toList();

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
                Arguments.of("axile.env.test.prop1", "fromTestSource"),
                Arguments.of("axile.env.test.prop2", "dynamicValue"),
                Arguments.of("axile.env.test.prop3", "fromCommandLine"));
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

        var propertyAppearances = response.getBody().propertySources().stream()
                .flatMap(src -> src.properties().stream()
                        .filter(p -> p.propertyName().equals(propertyName))
                        .map(p -> Map.entry(src.sourceName(), p)))
                .toList();

        assertThat(propertyAppearances)
                .extracting(e -> e.getValue().configPropsBeanName())
                .containsOnly(AxilePropTest.class.getName());
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("axile.prop.test.tags.environment"),
                Arguments.of("axile.prop.test.tags.version"),
                Arguments.of("axile.prop.test.enabled-contexts"),
                Arguments.of("axile.prop.test.http-client.requests[0].name"),
                Arguments.of("axile.prop.test.http-client.requests[0].base-url"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[0].type"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[0].retries[0].count"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[1].type"),
                Arguments.of("axile.prop.test.http-client.requests[1].name"),
                Arguments.of("axile.prop.test.http-client.requests[1].base-url"),
                Arguments.of("axile.prop.test.http-client.requests[1].methods[0].type"),
                Arguments.of("axile.prop.test.http-client.requests[1].methods[0].retries[0].count"),
                Arguments.of("axile.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level"));
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

    @ConfigurationProperties(prefix = "axile.prop.test")
    public record AxilePropTest(Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class AxileEnvironmentEndpointTestConfiguration {

        @Bean
        public AxileEnvironmentEndpoint axileEnvironmentEndpoint(
                EnvironmentEndpoint delegate, EnvPropertyEnricher envPropertyEnricher) {
            return new AxileEnvironmentEndpoint(delegate, envPropertyEnricher);
        }
    }
}
