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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;
import com.axelixlabs.axelix.sbs.spring.shared.AbstractEndpointIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Artemiy Degtyarev
 */
public class AxelixConfigurationPropertiesEndpointTest extends AbstractEndpointIntegrationTest {

    @ParameterizedTest
    @MethodSource("propertiesFeed")
    void shouldReturnPropertiesNameAndValue_forNonAdminRoles(String propertyName, String expectedValue, Role role) {
        ResponseEntity<ConfigurationPropertiesFeed> response = testRestTemplate
                .withRole(role)
                .getForEntity("/actuator/axelix-configprops", ConfigurationPropertiesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // The shared context binds the 'axelix.prop.test' prefix with several @ConfigurationProperties beans
        // (this record plus the beans/env endpoints' fixtures), so scope the feed to this test's own bean.
        List<KeyValue> properties = response.getBody().getBeans().stream()
                .filter(beans -> beans.getPrefix().equals("axelix.prop.test"))
                .filter(beans -> beans.getBeanName().contains(AxelixConfigurationProperties.class.getName()))
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
    @EnableConfigurationProperties(AxelixConfigurationProperties.class)
    public static class AxelixConfigurationPropertiesEndpointTestConfiguration {

        // The flattener / converter / propertyNameNormalizer / requiredAuthorityCheckService and the merged
        // SmartSanitizingFunction are provided by the shared configuration (EnvironmentTestConfig +
        // SharedEndpointTestConfiguration), so only this endpoint's own service + endpoint beans remain here.

        // @Primary so it wins over EnvironmentTestConfig#configurationPropertiesService for any
        // ConfigurationPropertiesService-by-type injection (e.g. the env property enricher's ObjectProvider).
        @Bean
        @Primary
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
