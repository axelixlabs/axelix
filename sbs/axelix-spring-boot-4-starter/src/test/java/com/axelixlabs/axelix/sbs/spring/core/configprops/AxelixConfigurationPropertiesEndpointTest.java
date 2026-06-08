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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.prop.test.tags.forSanitization=toBeSanitized",
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized",
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
@EnableConfigurationProperties({AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties.class})
@Import(JwtAuthTestConfiguration.class)
public class AxelixConfigurationPropertiesEndpointTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @ParameterizedTest
    @MethodSource("propertiesFeed")
    void shouldReturnPropertiesNameAndValue_forNonAdminRoles(String propertyName, String expectedValue, Role role) {
        ResponseEntity<ConfigurationPropertiesFeed> response = restTemplate
                .withRole(role)
                .getForEntity("/actuator/axelix-configprops", ConfigurationPropertiesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<KeyValue> properties = response.getBody().getBeans().stream()
                .filter(beans -> beans.getPrefix().equals("axelix.prop.test"))
                .flatMap(bean -> bean.getProperties().stream())
                .toList();

        assertThat(properties)
                .filteredOn(e -> e.getKey().equals(propertyName))
                .extracting(KeyValue::getValue)
                .containsExactly(expectedValue);
    }

    private static Stream<Arguments> propertiesFeed() {
        return Stream.of(
                Arguments.of("tags.forSanitization", "******", DefaultRole.VIEWER),
                Arguments.of("tags.FOR_SANITIZATION", "******", DefaultRole.VIEWER),
                Arguments.of("tags.forSanitization", "******", DefaultRole.EDITOR),
                Arguments.of("tags.FOR_SANITIZATION", "******", DefaultRole.EDITOR),
                Arguments.of("tags.forSanitization", "toBeSanitized", DefaultRole.ADMIN),
                Arguments.of("tags.FOR_SANITIZATION", "toBeSanitized", DefaultRole.ADMIN),
                Arguments.of("tags.version", "1.0.0", DefaultRole.VIEWER),
                Arguments.of("enabledContexts[0]", "user-service", DefaultRole.VIEWER),
                Arguments.of("enabledContexts[1]", "payment-service", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[0].name", "user-api", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[0].baseUrl", "https://api.users.example.com/v1", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[0].methods[0].type", "GET", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].count", "3", DefaultRole.VIEWER),
                Arguments.of(
                        "httpClient.requests[0].methods[0].retries[0].parameters.timeout", "5000", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[0].methods[1].type", "POST", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[1].name", "payment-api", DefaultRole.VIEWER),
                Arguments.of(
                        "httpClient.requests[1].baseUrl", "https://api.payments.example.com/v2", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[1].methods[0].type", "PUT", DefaultRole.VIEWER),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].count", "2", DefaultRole.VIEWER),
                Arguments.of(
                        "httpClient.requests[1].methods[0].retries[0].parameters.log-level",
                        "DEBUG",
                        DefaultRole.VIEWER));
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-configprops")
    void negativeAuthTests() {}

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public record AxelixConfigurationProperties(
            Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class AxelixConfigurationPropertiesEndpointTestConfiguration {

        @Bean
        public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
            return new DefaultConfigurationPropertiesFlattener();
        }

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter(
                ConfigurationPropertiesFlattener configurationPropertiesFlattener) {
            return new DefaultConfigurationPropertiesConverter(configurationPropertiesFlattener);
        }

        @Bean
        public PropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultPropertyNameNormalizer();
        }

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(
                    List.of("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION"),
                    propertyNameNormalizer);
        }

        @Bean
        public RequiredAuthorityCheckService requiredAuthorityCheckService(
                SecurityContextExecutor securityContextExecutor) {
            return new RequiredAuthorityCheckService(securityContextExecutor);
        }

        @Bean
        public DefaultConfigurationPropertiesService configurationPropertiesCache(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter,
                RequiredAuthorityCheckService requiredAuthorityCheckService) {
            return new DefaultConfigurationPropertiesService(
                    smartSanitizingFunction,
                    applicationContext,
                    configurationPropertiesConverter,
                    requiredAuthorityCheckService);
        }

        @Bean
        public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
                DefaultConfigurationPropertiesService configurationPropertiesService) {
            return new AxelixConfigurationPropertiesEndpoint(configurationPropertiesService);
        }
    }
}
