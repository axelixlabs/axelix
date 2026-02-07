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
package com.axelixlabs.axelix.master.api;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.PropertyManagementApi;
import com.axelixlabs.axelix.master.api.external.request.PropertyUpdatedRequest;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PropertyManagementApi}.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PropertyManagementApiTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-property-management")) {
                    return new MockResponse()
                            .setResponseCode(204)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(TestObjectFactory.createInstance(
                activeInstanceId,
                mockWebServer.url(activeInstanceId + "/actuator").toString(),
                Instance.InstanceStatus.UP));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnOkOnPropertyUpdate() {
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/property-management/{instanceId}",
                        defaultEntity(request),
                        Void.class,
                        activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Instance instanceModify =
                registry.get(InstanceId.of(activeInstanceId)).orElseThrow(InstanceNotFoundException::new);
        assertThat(instanceModify.status()).isEqualTo(Instance.InstanceStatus.RELOAD);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when updating property")
    void shouldReturnInternalServerErrorOnPropertyManagement() {
        String instanceId = UUID.randomUUID().toString();
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "value");
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/property-management/{instanceId}",
                        defaultEntity(request),
                        EndpointInvocationException.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/property-management/{instanceId}",
                        request,
                        EndpointInvocationException.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        PropertyUpdatedRequest request = new PropertyUpdatedRequest("property.enabled", "false");

        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/axelix/api/external/property-management/{instanceId}",
                        defaultEntity(request),
                        Void.class,
                        activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private HttpEntity<PropertyUpdatedRequest> defaultEntity(PropertyUpdatedRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }
}
