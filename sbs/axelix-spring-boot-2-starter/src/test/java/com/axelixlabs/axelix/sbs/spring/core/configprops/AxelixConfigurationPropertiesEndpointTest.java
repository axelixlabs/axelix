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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.sbs.spring.core.AbstractEndpointIntegrationTest;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class AxelixConfigurationPropertiesEndpointTest extends AbstractEndpointIntegrationTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @ParameterizedTest
    @MethodSource("propertiesFeed")
    void shouldReturnPropertiesNameAndValue_forNonAdminRoles(String propertyName, String expectedValue, Role role) {
        ResponseEntity<ConfigurationPropertiesFeed> response = restTemplate
                .withRole(role)
                .getForEntity("/actuator/axelix-configprops", ConfigurationPropertiesFeed.class);

        List<KeyValue> properties = response.getBody().getBeans().stream()
                .filter(bean -> bean.getPrefix().equals("axelix.prop.test"))
                .flatMap(bean -> bean.getProperties().stream())
                .collect(Collectors.toList());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(properties)
                .filteredOn(e -> e.getKey().equals(propertyName))
                .extracting(KeyValue::getValue)
                .containsExactly(expectedValue);
    }

    @ProtectedEndpointTests(
            method = com.axelixlabs.axelix.common.domain.http.HttpMethod.GET,
            path = "/actuator/axelix-configprops")
    void negativeAuthTests() {}

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
}
