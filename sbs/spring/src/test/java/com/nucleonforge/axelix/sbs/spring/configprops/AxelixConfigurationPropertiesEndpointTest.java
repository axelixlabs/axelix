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
package com.nucleonforge.axelix.sbs.spring.configprops;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axelix.common.api.ConfigPropsFeed;
import com.nucleonforge.axelix.common.api.KeyValue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.configprops.show-values=always")
@TestPropertySource(
        properties = {
            "axelix.prop.test.tags.environment=test",
            "axelix.prop.test.tags.version=1.0.0",
            "axelix.prop.test.enabled-contexts=user-service, payment-service",
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
@EnableConfigurationProperties(AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties.class)
public class AxelixConfigurationPropertiesEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @ParameterizedTest
    @MethodSource("propertyName")
    void shouldReturnPropertiesNameAndValue(String propertyName, String expectedValue) {
        ResponseEntity<ConfigPropsFeed> response =
                restTemplate.getForEntity("/actuator/axelix-configprops", ConfigPropsFeed.class);

        List<KeyValue> properties = response.getBody().contexts().values().stream()
                .flatMap(ctx -> ctx.beans().values().stream())
                .filter(e -> e.prefix().equals("axelix.prop.test"))
                .flatMap(bean -> bean.properties().stream())
                .toList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(properties)
                .filteredOn(e -> e.key().equals(propertyName))
                .extracting(KeyValue::value)
                .containsExactly(expectedValue);
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("tags.environment", "test"),
                Arguments.of("tags.version", "1.0.0"),
                Arguments.of("enabledContexts[0]", "user-service"),
                Arguments.of("enabledContexts[1]", "payment-service"),
                Arguments.of("httpClient.requests[0].name", "user-api"),
                Arguments.of("httpClient.requests[0].baseUrl", "https://api.users.example.com/v1"),
                Arguments.of("httpClient.requests[0].methods[0].type", "GET"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].count", "3"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].parameters.timeout", "5000"),
                Arguments.of("httpClient.requests[0].methods[1].type", "POST"),
                Arguments.of("httpClient.requests[1].name", "payment-api"),
                Arguments.of("httpClient.requests[1].baseUrl", "https://api.payments.example.com/v2"),
                Arguments.of("httpClient.requests[1].methods[0].type", "PUT"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].count", "2"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].parameters.log-level", "DEBUG"));
    }

    @ParameterizedTest
    @MethodSource("inputsName")
    void shouldReturnInputsName(String inputsName) {
        ResponseEntity<ConfigPropsFeed> response =
                restTemplate.getForEntity("/actuator/axelix-configprops", ConfigPropsFeed.class);

        List<KeyValue> inputs = response.getBody().contexts().values().stream()
                .flatMap(ctx -> ctx.beans().values().stream())
                .filter(e -> e.prefix().equals("axelix.prop.test"))
                .flatMap(bean -> bean.inputs().stream())
                .toList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(inputs).hasSize(30).extracting(KeyValue::key).contains(inputsName);
    }

    private static Stream<Arguments> inputsName() {
        return Stream.of(
                Arguments.of("tags.environment.value"),
                Arguments.of("tags.version.origin"),
                Arguments.of("enabledContexts[0].value"),
                Arguments.of("enabledContexts[1].value"),
                Arguments.of("httpClient.requests[0].name.origin"),
                Arguments.of("httpClient.requests[0].baseUrl.value"),
                Arguments.of("httpClient.requests[0].methods[0].type.origin"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].count.origin"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].parameters.timeout.origin"),
                Arguments.of("httpClient.requests[0].methods[1].type.value"),
                Arguments.of("httpClient.requests[1].name.value"),
                Arguments.of("httpClient.requests[1].baseUrl.origin"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].count.value"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].parameters.log-level.value"));
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public record AxelixConfigurationProperties(
            Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class AxelixConfigurationPropertiesTestConfiguration {

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter() {
            return new DefaultConfigurationPropertiesConverter();
        }

        @Bean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new ConfigurationPropertiesCache(
                    configurationPropertiesReportEndpoint, configurationPropertiesConverter);
        }

        @Bean
        public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
                ConfigurationPropertiesCache configurationPropertiesCache) {
            return new AxelixConfigurationPropertiesEndpoint(configurationPropertiesCache);
        }
    }
}
