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
package com.axelixlabs.axelix.sbs.spring.properties;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultPropertyNameDiscoverer}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.prop.test.dynamic-properties=dynamicValue",
            "axelix.prop.test.enabled-contexts=user-service, payment-service",
            "axelix.prop.test.http-client.requests[0].name=user-api",
            "axelix.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axelix.prop.test.http-client.requests[0].methods[0].type=GET",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "AXELIX_PROP_TEST_HTTP_CLIENT_REQUESTS_0_METHODS_1_TYPE=POST",
            "axelix.prop.test.http-client.requests[1].name=payment-api",
            "axelix.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axelix.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
public class DefaultPropertyNameDiscovererTest {

    @Autowired
    private PropertyNameDiscoverer discoverer;

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axelix.prop.test.dynamicProperties", () -> "dynamicValue");
    }

    @ParameterizedTest
    @MethodSource("propertyName")
    void should(String discoverProperty, String expectedProperty) {
        assertThat(discoverer.discover(discoverProperty)).isEqualTo(expectedProperty);
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("axelix.property.not-found", null),
                Arguments.of("axelix.prop.test.dynamic-properties", "axelix.prop.test.dynamicProperties"),
                Arguments.of("AXELIX_PROP_TEST_ENABLED_CONTEXTS", "axelix.prop.test.enabled-contexts"),
                Arguments.of(
                        "axelix.prop.test.httpClient.requests.name", "axelix.prop.test.http-client.requests[0].name"),
                Arguments.of(
                        "axelix.prop.test.httpClient.requests.baseUrl",
                        "axelix.prop.test.http-client.requests[0].base-url"),
                Arguments.of(
                        "axelix.prop.test.httpClient.requests.methods.type",
                        "axelix.prop.test.http-client.requests[0].methods[0].type"),
                Arguments.of(
                        "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count",
                        "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count"),
                Arguments.of(
                        "axelix.prop.test.httpClient.requests.methods.retries.parameters.timeout",
                        "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout"),
                Arguments.of(
                        "AXELIX_PROP_TEST_HTTP_CLIENT_REQUESTS_METHODS_1_TYPE",
                        "AXELIX_PROP_TEST_HTTP_CLIENT_REQUESTS_0_METHODS_1_TYPE"),
                Arguments.of(
                        "axelix.prop.test.httpClient.requests[1].name",
                        "axelix.prop.test.http-client.requests[1].name"),
                Arguments.of(
                        "axelix.prop.test.http-client.requests[1].base-url",
                        "axelix.prop.test.http-client.requests[1].base-url"),
                Arguments.of(
                        "axelix.prop.test.httpClient.requests[1].methods.type",
                        "axelix.prop.test.http-client.requests[1].methods[0].type"),
                Arguments.of(
                        "axelix.prop.test.http-client.requests[1].methods.retries[0].count",
                        "axelix.prop.test.http-client.requests[1].methods[0].retries[0].count"),
                Arguments.of(
                        "axelix.prop.test.httpClient.requests[1].methods.retries.parameters.logLevel",
                        "axelix.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level"));
    }

    @TestConfiguration
    static class DefaultPropertyDiscovererTestConfiguration {

        @Bean
        public PropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultPropertyNameNormalizer();
        }

        @Bean
        public PropertyNameDiscoverer propertyNameDiscoverer(
                ConfigurableEnvironment configurableEnvironment, PropertyNameNormalizer propertyNameNormalizer) {
            return new DefaultPropertyNameDiscoverer(configurableEnvironment, propertyNameNormalizer);
        }
    }
}
