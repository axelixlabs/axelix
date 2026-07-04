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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.api.external.request.loggers.LogLevelLoggerBulkChangeRequest;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LoggersApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoggersApiManagementLoggingLevelTest {

    private static final String LOG_LEVEL_GROUP_AUTH_JSON = "{\"configuredLevel\":\"INFO\"}";

    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String siblingInstanceId = UUID.randomUUID().toString();
    private static final String failingInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    private List<String> invokedPaths;

    private AtomicInteger failingLoggerUpdateStatusCode;

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
        invokedPaths = new CopyOnWriteArrayList<>();
        failingLoggerUpdateStatusCode = new AtomicInteger(500);
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;
                invokedPaths.add(path);

                if (path.equals("/" + activeInstanceId + "/axelix-loggers/group/groupName/change-level")) {
                    return new MockResponse();
                }
                if (path.equals("/" + activeInstanceId + "/axelix-loggers/logger/reset.logger.name/reset")) {
                    return new MockResponse();
                }
                if (path.equals("/" + failingInstanceId + "/axelix-loggers/logger/logger.name/change-level")) {
                    return new MockResponse().setResponseCode(failingLoggerUpdateStatusCode.get());
                }
                if (path.equals("/" + activeInstanceId + "/axelix-loggers/logger/logger.name/change-level")
                        || path.equals("/" + siblingInstanceId + "/axelix-loggers/logger/logger.name/change-level")) {
                    return new MockResponse();
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        registry.reload(TestInstanceFactory.create(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));
        registry.reload(TestInstanceFactory.create(
                siblingInstanceId, mockWebServer.url(siblingInstanceId).toString()));
        registry.reload(TestInstanceFactory.create(
                failingInstanceId, mockWebServer.url(failingInstanceId).toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
        registry.deRegister(InstanceId.of(siblingInstanceId));
        registry.deRegister(InstanceId.of(failingInstanceId));
    }

    @Test
    void shouldSetLoggingLevelByGroupName() {
        String groupName = "groupName";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO", null);

        // when.
        ResponseEntity<String> body = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/loggers/{instanceId}/group/{groupName}",
                        requestBody,
                        String.class,
                        activeInstanceId,
                        groupName);

        // then.
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldSetLoggingLevelByLoggerNameAcrossInstances() {
        // given.
        LogLevelLoggerBulkChangeRequest requestBody = new LogLevelLoggerBulkChangeRequest(
                List.of(activeInstanceId, siblingInstanceId), "logger.name", null, "DEBUG");

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(invokedPaths)
                .containsExactlyInAnyOrder(
                        "/" + activeInstanceId + "/axelix-loggers/logger/logger.name/change-level",
                        "/" + siblingInstanceId + "/axelix-loggers/logger/logger.name/change-level");
    }

    @Test
    void shouldResetLoggingLevelByLoggerName() {
        String loggerName = "reset.logger.name";

        // when
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/loggers/{instanceId}/logger/{loggerName}/reset",
                        null,
                        String.class,
                        activeInstanceId,
                        loggerName);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 500})
    void shouldReturnBadRequestAndInvokeAllInstances_WhenLoggerUpdatePartiallyFails(int failureStatusCode) {
        // given.
        failingLoggerUpdateStatusCode.set(failureStatusCode);
        LogLevelLoggerBulkChangeRequest requestBody = new LogLevelLoggerBulkChangeRequest(
                List.of(activeInstanceId, failingInstanceId), "logger.name", null, "DEBUG");

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
        assertThat(invokedPaths)
                .containsExactlyInAnyOrder(
                        "/" + activeInstanceId + "/axelix-loggers/logger/logger.name/change-level",
                        "/" + failingInstanceId + "/axelix-loggers/logger/logger.name/change-level");
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_WhenInvokedOnUnknownInstance() {
        String instanceId = UUID.randomUUID().toString();
        String groupName = "groupName";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO", null);
        registry.reload(TestInstanceFactory.create(instanceId));

        // when.
        ResponseEntity<?> response = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/loggers/{instanceId}/group/{groupName}",
                        requestBody,
                        Void.class,
                        instanceId,
                        groupName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_WhenResettingOnUnknonInstance() {
        String instanceId = UUID.randomUUID().toString();
        String loggerName = "reset.logger.name";
        registry.reload(TestInstanceFactory.create(instanceId));

        // when.
        ResponseEntity<?> response = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/loggers/{instanceId}/logger/{loggerName}/reset",
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
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO", null);

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/loggers/{instanceId}/group/{groupName}",
                        requestBody,
                        String.class,
                        instanceId,
                        groupName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnLoggerBulkChange() {
        // given.
        String instanceId = "unregistered-loggers-logger-instance";
        LogLevelLoggerBulkChangeRequest requestBody =
                new LogLevelLoggerBulkChangeRequest(List.of(instanceId), "logger.name", null, "DEBUG");

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @Test
    void givenInstanceIdsAreEmpty_shouldReturnBadRequest_WhenUpdatingLoggingLevelBulk() {
        // given.
        LogLevelLoggerBulkChangeRequest requestBody =
                new LogLevelLoggerBulkChangeRequest(List.of(), "logger.name", null, "DEBUG");

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @Test
    void shouldDeduplicateInstanceIds_WhenUpdatingLoggingLevelBulk() {
        // given.
        LogLevelLoggerBulkChangeRequest requestBody = new LogLevelLoggerBulkChangeRequest(
                List.of(activeInstanceId, activeInstanceId), "logger.name", null, "DEBUG");

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(invokedPaths)
                .containsExactly("/" + activeInstanceId + "/axelix-loggers/logger/logger.name/change-level");
    }

    @Test
    void givenConfiguredLevelIsEmpty_shouldReturnBadRequest__WhenUpdatingLoggingLevelBulk() {
        // given.
        LogLevelLoggerBulkChangeRequest requestBody =
                new LogLevelLoggerBulkChangeRequest(List.of(activeInstanceId), "logger.name", null, " ");

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ProtectedEndpointTests(
            method = HttpMethod.POST,
            path = "/api/external/loggers/00000000-0000-0000-0000-000000000001/group/groupName",
            jsonBody = LOG_LEVEL_GROUP_AUTH_JSON)
    void negativeAuthTestsOnGroupName() {}

    @ProtectedEndpointTests(method = HttpMethod.POST, path = "/api/external/loggers/logger", jsonBody = """
                    {
                      "instanceIds": ["00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000001"],
                      "loggerName": "logger.name",
                      "configuredLevel": "DEBUG"
                    }
                    """)
    void negativeAuthTestsOnLoggerName() {}

    @ProtectedEndpointTests(
            method = HttpMethod.POST,
            path = "/api/external/loggers/00000000-0000-0000-0000-000000000001/logger/reset.logger.name/reset")
    void negativeAuthTestsOnResetLoggingLevelByLoggerName() {}
}
