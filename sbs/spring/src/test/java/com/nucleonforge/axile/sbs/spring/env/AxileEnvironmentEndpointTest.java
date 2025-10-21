package com.nucleonforge.axile.sbs.spring.env;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxileEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--axile.env.test.prop3=fromCommandLine"},
        properties = {
            "axile.env.test.prop1=systemValue1",
            "axile.env.test.prop2=systemValue2",
            "management.endpoint.env.show-values=always"
        })
@TestPropertySource(properties = {"axile.env.test.prop1=fromTestSource"})
class AxileEnvironmentEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfigurableEnvironment environment;

    @BeforeEach
    void before() {
        environment.getSystemProperties().put("axile.env.test.prop2", "systemValue");
    }

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axile.env.test.prop2", () -> "dynamicValue");
    }

    @ParameterizedTest(name = "Property ''{0}'' should resolve from highest-precedence source")
    @MethodSource("propertyExpectations")
    void shouldSelectPrimaryPropertyFromHighestPrecedenceSource(String propertyName, String expectedValue) {
        ResponseEntity<AxileEnvironmentEndpoint.AxileEnvironmentDescriptor> response = restTemplate.getForEntity(
                "/actuator/axile-env", AxileEnvironmentEndpoint.AxileEnvironmentDescriptor.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        var body = response.getBody();
        assertThat(body).isNotNull();

        var propertyAppearances = body.propertySources().stream()
                .flatMap(src -> src.properties().entrySet().stream()
                        .filter(e -> e.getKey().equals(propertyName))
                        .map(e -> Map.entry(src.name(), e.getValue())))
                .toList();

        assertThat(propertyAppearances).isNotEmpty();

        var primary = propertyAppearances.stream()
                .filter(e -> e.getValue().isPrimary())
                .findFirst()
                .orElseThrow();

        assertThat(primary.getValue().value()).isEqualTo(expectedValue);

        if (propertyAppearances.size() > 1) {
            assertThat(propertyAppearances.stream().anyMatch(e -> !e.getValue().isPrimary()))
                    .isTrue();
        }
    }

    private static Stream<Arguments> propertyExpectations() {
        return Stream.of(
                Arguments.of("axile.env.test.prop1", "fromTestSource"),
                Arguments.of("axile.env.test.prop2", "dynamicValue"),
                Arguments.of("axile.env.test.prop3", "fromCommandLine"));
    }

    @Test
    void shouldReturnValidJsonStructure() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axile-env", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        assertThatJson(responseBody).node("activeProfiles").isNotNull().isArray();

        assertThatJson(responseBody).node("defaultProfiles").isNotNull().isArray();

        assertThatJson(responseBody)
                .inPath("propertySources[*].properties")
                .isArray()
                .allSatisfy(properties -> assertThatJson(properties)
                        .isObject()
                        .allSatisfy((propertyName, propertyValue) -> assertThatJson(propertyValue)
                                .isObject()
                                .containsKey("isPrimary")
                                .node("isPrimary")
                                .isBoolean()));
    }

    @TestConfiguration
    static class AxileEnvironmentEndpointTestConfiguration {

        @Bean
        public EnvPropertyEnricher envPropertyEnricher(Environment environment) {
            return new DefaultEnvPropertyEnricher(environment);
        }

        @Bean
        public AxileEnvironmentEndpoint axileEnvironmentEndpoint(
                EnvironmentEndpoint delegate, EnvPropertyEnricher envPropertyEnricher) {
            return new AxileEnvironmentEndpoint(delegate, envPropertyEnricher);
        }
    }
}
