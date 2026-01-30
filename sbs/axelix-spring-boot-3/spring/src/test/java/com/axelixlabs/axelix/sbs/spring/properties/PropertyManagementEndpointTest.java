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

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.axelixlabs.axelix.sbs.spring.context.DefaultContextRestarter;
import com.axelixlabs.axelix.sbs.spring.context.RestartListener;
import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.utils.ContextKeepAliveTestListener;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PropertyManagementEndpoint} using {@link TestRestTemplate}
 * and a real HTTP context with web environment.
 *
 * <p>These tests verify that the actuator endpoint {@code /actuator/property-management}
 * correctly handles property mutation operations, including validation of input parameters
 * and successful updates of property values.</p>
 *
 * <p>To be discoverable and enabled during tests, the actuator endpoint should either be:
 * <ul>
 *     <li>Explicitly included via {@code management.endpoints.web.exposure.include=property-management}, or</li>
 *     <li>Configured as part of auto-configuration in the test application context.</li>
 * </ul>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.env.show-values=always")
@TestExecutionListeners(
        listeners = {
            DependencyInjectionTestExecutionListener.class,
            DirtiesContextTestExecutionListener.class,
            ContextKeepAliveTestListener.class
        })
@TestPropertySource(
        properties = {
            "axelix.my-empty.property= ",
            "axelix.not-empty.property=not-empty",
            "axelix.prop.test.dynamic-properties=old-dynamic-value",
            "AXELIX_ENABLED_CONTEXT=old-value-context",
            "axelix.http-client.requests[0].name=old-value-name",
            "axelix.http-client.requests[0].base-url=old-value-baseUrl",
            "axelix.http-client.requests[0].methods[0].type=old-value-type1",
            "axelix.http-client.requests[0].methods[0].retries[0].count=old-value-count1",
            "axelix.http-client.requests[0].methods[0].retries[0].parameters.timeout=old-value-timeout",
            "axelix.http-client.requests[0].methods[1].type=old-value-type2",
            "axelix.http-client.requests[1].methods[0].type=old-value-type3",
            "axelix.http-client.requests[1].methods[0].retries[0].count=old-value-count2",
            "axelix.http-client.requests[1].methods[0].retries[0].parameters.log-level=old-value-logLevel"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({
    PropertyManagementEndpoint.class,
    ContextReloadingPropertyMutator.class,
    DefaultPropertyNameNormalizer.class,
    DefaultPropertyNameDiscoverer.class,
    DefaultContextRestarter.class,
    RestartListener.class
})
class PropertyManagementEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axelix.prop.test.dynamicProperties", () -> "new-dynamic-value");
    }

    @ParameterizedTest
    @MethodSource("updateProperty")
    void mutate_shouldUpdatePropertyValue(String envProperty, String mutateProperty, String newValue)
            throws InterruptedException {
        mutateProperty(mutateProperty, newValue);

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/" + envProperty, Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo(newValue);
    }

    private static Stream<Arguments> updateProperty() {
        return Stream.of(
                Arguments.of("axelix.my-empty.property", "axelix.myEmpty.property", "new-value"),
                Arguments.of("axelix.not-empty.property", "axelix.notEmpty.property", ""),
                Arguments.of(
                        "axelix.prop.test.dynamicProperties",
                        "axelix.prop.test.dynamic-properties",
                        "new-dynamic-value"),
                Arguments.of("AXELIX_ENABLED_CONTEXT", "AXELIX_ENABLED_CONTEXT", "new-value-context"),
                Arguments.of(
                        "axelix.http-client.requests[0].name", "axelix.httpClient.requests.name", "new-value-name"),
                Arguments.of(
                        "axelix.http-client.requests[0].base-url",
                        "axelix.httpClient.requests[0].baseUrl",
                        "new-value-baseUrl"),
                Arguments.of(
                        "axelix.http-client.requests[0].methods[0].type",
                        "axelix.httpClient.requests.methods.type",
                        "new-value-type1"),
                Arguments.of(
                        "axelix.http-client.requests[0].methods[0].retries[0].count",
                        "axelix.httpClient.requests[0].methods[0].retries[0].count",
                        "new-value-count1"),
                Arguments.of(
                        "axelix.http-client.requests[0].methods[0].retries[0].parameters.timeout",
                        "axelix.httpClient.requests.methods[0].retries.parameters.timeout",
                        "new-value-timeout"),
                Arguments.of(
                        "axelix.http-client.requests[0].methods[1].type",
                        "axelix.httpClient.requests.methods[1].type",
                        "new-value-type2"),
                Arguments.of(
                        "axelix.http-client.requests[1].methods[0].type",
                        "axelix.httpClient.requests[1].Methods.type",
                        "new-value-type3"),
                Arguments.of(
                        "axelix.http-client.requests[1].methods[0].retries[0].count",
                        "axelix.httpClient.requests[1].methods.retries[0].count",
                        "new-value-count2"),
                Arguments.of(
                        "axelix.http-client.requests[1].methods[0].retries[0].parameters.log-level",
                        "axelix.httpClient.requests[1].methods.retries.parameters.logLevel",
                        "new-value-logLevel"));
    }

    @ParameterizedTest
    @MethodSource("emptyPropertyName")
    void mutate_shouldReturnBadRequest_whenPropertyNameIsEmpty(String emptyProperty) {
        PropertyMutationRequest request = new PropertyMutationRequest(emptyProperty, "someValue");

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    private static Stream<Arguments> emptyPropertyName() {
        return Stream.of(Arguments.of(""), Arguments.of(" "), Arguments.of("\t"));
    }

    @ParameterizedTest
    @MethodSource("newProperty")
    void mutate_shouldCreateNewProperty_whenPropertyDoesNotExist(String newProperty, String value)
            throws InterruptedException {
        Map<?, ?> initialResponse = restTemplate.getForObject("/actuator/env/" + newProperty, Map.class);
        assertThat(initialResponse).isNull();

        mutateProperty(newProperty, value);

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/" + newProperty, Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo(value);
    }

    private static Stream<Arguments> newProperty() {
        return Stream.of(
                Arguments.of("non-existent-property.property", "value1"),
                Arguments.of("non_$tandard.property", "value2"));
    }

    /**
     * Mutates the value of a property using the custom Actuator endpoint and waits for the update to take effect.
     *
     * @param propertyName name of the property to mutate
     * @param newValue     new value to set (can be blank or non-blank)
     */
    private void mutateProperty(String propertyName, String newValue) throws InterruptedException {
        PropertyMutationRequest request = new PropertyMutationRequest(propertyName, newValue);

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        TimeUnit.SECONDS.sleep(7); // wait for context update
        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private HttpEntity<PropertyMutationRequest> defaultEntity(PropertyMutationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private String path() {
        return "/actuator/axelix-property-management";
    }
}
