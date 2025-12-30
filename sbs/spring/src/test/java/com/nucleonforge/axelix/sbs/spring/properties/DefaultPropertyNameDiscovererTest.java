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
package com.nucleonforge.axelix.sbs.spring.properties;

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

import com.nucleonforge.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.nucleonforge.axelix.sbs.spring.env.PropertyNameNormalizer;

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
