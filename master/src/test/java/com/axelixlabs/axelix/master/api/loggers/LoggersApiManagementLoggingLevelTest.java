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
package com.axelixlabs.axelix.master.api.loggers;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.LoggersApi;
import com.axelixlabs.axelix.master.api.external.request.LogLevelChangeRequest;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LoggersApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoggersApiManagementLoggingLevelTest {
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

                if (path.equals("/" + activeInstanceId + "/loggers/groupName")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/loggers/logger.name")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/loggers/clear.logger.name")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(TestObjectFactory.createInstance(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldSetLoggingLevelByGroupName() {
        String groupName = "groupName";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO");

        // when.
        ResponseEntity<String> body = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/group/{groupName}",
                        requestBody,
                        String.class,
                        activeInstanceId,
                        groupName);

        // then.
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldSetLoggingLevelByLoggerName() {
        String loggerName = "logger.name";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("DEBUG");

        // when.
        ResponseEntity<String> body = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}",
                        requestBody,
                        String.class,
                        activeInstanceId,
                        loggerName);

        // then.
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldClearLoggingLevelByLoggerName() {
        String loggerName = "clear.logger.name";

        // when
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}/clear",
                        null,
                        String.class,
                        activeInstanceId,
                        loggerName);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnGroupName() {
        String instanceId = UUID.randomUUID().toString();
        String groupName = "groupName";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO");
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<?> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/group/{groupName}",
                        requestBody,
                        Void.class,
                        instanceId,
                        groupName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnLoggerName() {
        String instanceId = UUID.randomUUID().toString();
        String loggerName = "logger.name";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("DEBUG");
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<?> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}",
                        requestBody,
                        Void.class,
                        instanceId,
                        loggerName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnClearLoggingLevelByLoggerName() {
        String instanceId = UUID.randomUUID().toString();
        String loggerName = "clear.logger.name";
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<?> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}/clear",
                        null,
                        Void.class,
                        instanceId,
                        loggerName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnGroupName() {
        String instanceId = "unregistered-loggers-group-instance";
        String groupName = "groupName";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO");

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/group/{groupName}",
                        requestBody,
                        EndpointInvocationException.class,
                        instanceId,
                        groupName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnLoggerName() {
        String instanceId = "unregistered-loggers-instance";
        String loggerName = "logger.name";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("DEBUG");

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}",
                        requestBody,
                        EndpointInvocationException.class,
                        instanceId,
                        loggerName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnClearLoggingLevelByLoggerName() {
        String instanceId = "unregistered-logger-instance";
        String loggerName = "clear.logger.name";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}/clear",
                        null,
                        EndpointInvocationException.class,
                        instanceId,
                        loggerName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnGroupName(InvalidAuthScenario scenario) {
        String groupName = "groupName";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO");

        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/group/{groupName}",
                        requestBody,
                        Void.class,
                        activeInstanceId,
                        groupName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnLoggerName(InvalidAuthScenario scenario) {
        String loggerName = "logger.name";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("DEBUG");

        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}",
                        requestBody,
                        Void.class,
                        activeInstanceId,
                        loggerName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnClearLoggingLevelByLoggerName(InvalidAuthScenario scenario) {
        String loggerName = "clear.logger.name";

        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/axelix/api/external/loggers/{instanceId}/logger/{loggerName}/clear",
                        null,
                        Void.class,
                        activeInstanceId,
                        loggerName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
