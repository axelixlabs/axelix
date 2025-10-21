package com.nucleonforge.axile.sbs.spring.env;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

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
            "management.endpoint.env.show-values=always",
        })
@TestPropertySource(properties = {"axile.env.test.prop1=fromTestSource"})
class AxileEnvironmentEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    ConfigurableEnvironment environment;

    @BeforeEach
    void before() {
        System.setProperty("axile.env.test.prop2", "systemValue");
        environment.getSystemProperties().put("axile.env.test.prop2", "systemValue");
    }

    @AfterEach
    void after() {
        System.clearProperty("axile.env.test.prop2");
    }

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axile.env.test.prop2", () -> "dynamicValue");
    }

    @Test
    void shouldMarkWinningPropertyFromHighestSource() {
        ResponseEntity<AxileEnvironmentEndpoint.EnvironmentDescriptor> response =
                restTemplate.getForEntity("/actuator/axile-env", AxileEnvironmentEndpoint.EnvironmentDescriptor.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        AxileEnvironmentEndpoint.EnvironmentDescriptor body = response.getBody();
        assertThat(body.propertySources()).isNotEmpty();

        var appearances = body.propertySources().stream()
                .flatMap(src -> src.properties().entrySet().stream()
                        .filter(e -> e.getKey().equals("axile.env.test.prop1"))
                        .map(e -> Map.entry(src.name(), e.getValue())))
                .toList();

        assertThat(appearances).hasSizeGreaterThanOrEqualTo(1);

        var winner = appearances.stream()
                .filter(e -> e.getValue().isPrimary())
                .findFirst()
                .orElseThrow();

        assertThat(winner.getValue().value()).isEqualTo("fromTestSource");
        assertThat(winner.getValue().isPrimary()).isTrue();

        appearances.stream().filter(e -> !e.getValue().isPrimary()).forEach(e -> assertThat(false)
                .isFalse());
    }

    @Test
    void shouldFallbackToLowerPriorityWhenSystemPropertyRemoved() {
        System.clearProperty("axile.env.test.prop2");

        ResponseEntity<AxileEnvironmentEndpoint.EnvironmentDescriptor> response =
                restTemplate.getForEntity("/actuator/axile-env", AxileEnvironmentEndpoint.EnvironmentDescriptor.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();

        var winner = body.propertySources().stream()
                .flatMap(src -> src.properties().entrySet().stream())
                .filter(e -> e.getKey().equals("axile.env.test.prop2")
                        && e.getValue().isPrimary())
                .findFirst()
                .orElseThrow();

        assertThat(winner.getValue().value()).isEqualTo("dynamicValue");
    }

    @Test
    void shouldPreferCommandLinePropertyOverYml() {
        Map<String, Object> ymlProps = Map.of("axile.env.test.prop3", "fromYml");
        environment.getPropertySources().addLast(new MapPropertySource("ymlProps", ymlProps));

        ResponseEntity<AxileEnvironmentEndpoint.EnvironmentDescriptor> response =
                restTemplate.getForEntity("/actuator/axile-env", AxileEnvironmentEndpoint.EnvironmentDescriptor.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();

        var appearances = body.propertySources().stream()
                .flatMap(src -> src.properties().entrySet().stream()
                        .filter(e -> e.getKey().equals("axile.env.test.prop3"))
                        .map(e -> Map.entry(src.name(), e.getValue())))
                .toList();

        assertThat(appearances).hasSizeGreaterThanOrEqualTo(2);

        var winner = appearances.stream()
                .filter(e -> e.getValue().isPrimary())
                .findFirst()
                .orElseThrow();

        assertThat(winner.getValue().value()).isEqualTo("fromCommandLine");
        assertThat(winner.getValue().isPrimary()).isTrue();

        assertThat(appearances.stream()
                        .filter(e -> !e.getValue().isPrimary())
                        .anyMatch(e -> Objects.equals(e.getValue().value(), "fromYml")))
                .isTrue();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AxileEnvironmentEndpoint axileEnvironmentEndpoint(EnvironmentEndpoint delegate) {
            return new AxileEnvironmentEndpoint(delegate);
        }
    }
}
