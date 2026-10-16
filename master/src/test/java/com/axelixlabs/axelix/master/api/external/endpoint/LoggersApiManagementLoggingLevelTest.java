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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.api.external.request.loggers.LogLevelLoggerBulkChangeRequest;
import com.axelixlabs.axelix.master.api.external.request.loggers.LogLevelLoggerBulkResetRequest;
import com.axelixlabs.axelix.master.contract.logger.GroupLogLevelChangeRequest;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LoggersApi}.
 *
 * @author Sergey Cherkasov
 */
public class LoggersApiManagementLoggingLevelTest extends AbstractProtectedEndpointTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String siblingInstanceId = UUID.randomUUID().toString();
    private static final String failingInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    private final List<String> invokedPaths = new CopyOnWriteArrayList<>();

    private final AtomicInteger failingLoggerUpdateStatusCode = new AtomicInteger(500);

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
                invokedPaths.add(path);

                if (path.equals("/" + activeInstanceId + "/axelix-loggers/group/groupName/change-level")) {
                    return new MockResponse();
                }
                if (path.equals("/" + activeInstanceId + "/axelix-loggers/logger/reset.logger.name/reset")
                        || path.equals("/" + siblingInstanceId + "/axelix-loggers/logger/reset.logger.name/reset")) {
                    return new MockResponse();
                }
                if (path.equals("/" + failingInstanceId + "/axelix-loggers/logger/logger.name/change-level")
                        || path.equals("/" + failingInstanceId + "/axelix-loggers/logger/reset.logger.name/reset")) {
                    return new MockResponse().setResponseCode(failingLoggerUpdateStatusCode.get());
                }
                if (path.equals("/" + activeInstanceId + "/axelix-loggers/logger/logger.name/change-level")
                        || path.equals("/" + siblingInstanceId + "/axelix-loggers/logger/logger.name/change-level")) {
                    return new MockResponse();
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        registry.register(TestInstanceFactory.create(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));
        registry.register(TestInstanceFactory.create(
                siblingInstanceId, mockWebServer.url(siblingInstanceId).toString()));
        registry.register(TestInstanceFactory.create(
                failingInstanceId, mockWebServer.url(failingInstanceId).toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
        registry.deRegister(InstanceId.of(siblingInstanceId));
        registry.deRegister(InstanceId.of(failingInstanceId));
        invokedPaths.clear();
        failingLoggerUpdateStatusCode.set(500);
    }

    @Nested
    class ChangeLoggerLevel {

        @Test
        void shouldSetLoggingLevelByLoggerNameAcrossInstances() {
            // given.
            LogLevelLoggerBulkChangeRequest requestBody = new LogLevelLoggerBulkChangeRequest(
                    List.of(activeInstanceId, siblingInstanceId), "logger.name", null, "DEBUG");

            // when.
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
            ResponseEntity<String> response =
                    viewer.postForEntity("/api/external/loggers/logger", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + activeInstanceId + "/axelix-loggers/logger/logger.name/change-level",
                            "/" + siblingInstanceId + "/axelix-loggers/logger/logger.name/change-level");
            assertSuccessfulCallback(MasterWebEndpoints.LOGGERS_BULK_CHANGE, viewer.getActor());
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 500})
        void shouldReturnBadRequestAndInvokeAllInstances_WhenLoggerUpdatePartiallyFails(int failureStatusCode) {
            // given.
            failingLoggerUpdateStatusCode.set(failureStatusCode);
            LogLevelLoggerBulkChangeRequest requestBody = new LogLevelLoggerBulkChangeRequest(
                    List.of(failingInstanceId, activeInstanceId), "logger.name", null, "DEBUG");

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

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRejectMissingInstanceIds_WhenUpdatingLoggingLevelBulk(List<String> instanceIds) {
            // given.
            // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", instanceIds);
            requestBody.put("loggerName", "logger.name");
            requestBody.put("configuredLevel", "DEBUG");

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
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
            ResponseEntity<String> response =
                    viewer.postForEntity("/api/external/loggers/logger", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactly("/" + activeInstanceId + "/axelix-loggers/logger/logger.name/change-level");
            assertSuccessfulCallback(MasterWebEndpoints.LOGGERS_BULK_CHANGE, viewer.getActor());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = " ")
        void shouldRejectMissingConfiguredLevel_WhenUpdatingLoggingLevelBulk(String configuredLevel) {
            // given.
            // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", List.of(activeInstanceId));
            requestBody.put("loggerName", "logger.name");
            requestBody.put("configuredLevel", configuredLevel);

            // when.
            ResponseEntity<String> response =
                    restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = " ")
        void shouldRejectMissingLoggerName_WhenUpdatingLoggingLevelBulk(String loggerName) {
            // given.
            // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", List.of(activeInstanceId));
            requestBody.put("loggerName", loggerName);
            requestBody.put("configuredLevel", "DEBUG");

            // when.
            ResponseEntity<String> response =
                    restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @Test
        void shouldRejectBlankInstanceIds_WhenUpdatingLoggingLevelBulk() {
            // given.
            // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
            Map<String, Object> requestBody =
                    Map.of("instanceIds", List.of("", " "), "loggerName", "logger.name", "configuredLevel", "DEBUG");

            // when.
            ResponseEntity<String> response =
                    restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @Test
        void shouldReturnBadRequestAndInvokeRemainingInstances_WhenUpdatingOnUnknownInstance() {
            // given.
            LogLevelLoggerBulkChangeRequest requestBody = new LogLevelLoggerBulkChangeRequest(
                    List.of("unknown-logger-instance", siblingInstanceId), "logger.name", null, "DEBUG");

            // when.
            ResponseEntity<String> response =
                    restTemplate.asViewer().postForEntity("/api/external/loggers/logger", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactly("/" + siblingInstanceId + "/axelix-loggers/logger/logger.name/change-level");
        }
    }

    @Nested
    class ChangeGroupLevel {

        @Test
        void shouldSetLoggingLevelByGroupName() {
            String groupName = "groupName";
            GroupLogLevelChangeRequest requestBody = new GroupLogLevelChangeRequest().configuredLevel("INFO");

            // when.
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
            ResponseEntity<String> body = viewer.postForEntity(
                    "/api/external/loggers/{instanceId}/group/{groupName}",
                    requestBody,
                    String.class,
                    activeInstanceId,
                    groupName);

            // then.
            assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertSuccessfulCallback(MasterWebEndpoints.LOGGER_GROUP_CHANGE, viewer.getActor());
        }

        @Test
        @DisplayName("Should return 500 on EndpointInvocationError")
        void shouldReturnInternalServerError_WhenInvokedOnUnknownInstance() {
            String instanceId = UUID.randomUUID().toString();
            String groupName = "groupName";
            GroupLogLevelChangeRequest requestBody = new GroupLogLevelChangeRequest().configuredLevel("INFO");
            registry.register(TestInstanceFactory.create(instanceId));

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
        void shouldReturnBadRequestForUnregisteredInstance_OnGroupName() {
            String instanceId = "unregistered-loggers-group-instance";
            String groupName = "groupName";
            GroupLogLevelChangeRequest requestBody = new GroupLogLevelChangeRequest().configuredLevel("INFO");

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
    }

    @Nested
    class ResetLoggerLevel {

        @Test
        void shouldResetLoggingLevelOnSingleInstance() {
            // given.
            LogLevelLoggerBulkResetRequest requestBody =
                    new LogLevelLoggerBulkResetRequest(List.of(activeInstanceId), "reset.logger.name");
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

            // when.
            ResponseEntity<String> response =
                    viewer.postForEntity("/api/external/loggers/logger/reset", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + activeInstanceId + "/axelix-loggers/logger/reset.logger.name/reset");
            assertSuccessfulCallback(MasterWebEndpoints.LOGGERS_BULK_RESET, viewer.getActor());
        }

        @Test
        void shouldResetLoggingLevelAcrossInstances() {
            // given.
            LogLevelLoggerBulkResetRequest requestBody = new LogLevelLoggerBulkResetRequest(
                    List.of(activeInstanceId, siblingInstanceId), "reset.logger.name");
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

            // when.
            ResponseEntity<String> response =
                    viewer.postForEntity("/api/external/loggers/logger/reset", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + activeInstanceId + "/axelix-loggers/logger/reset.logger.name/reset",
                            "/" + siblingInstanceId + "/axelix-loggers/logger/reset.logger.name/reset");
            assertSuccessfulCallback(MasterWebEndpoints.LOGGERS_BULK_RESET, viewer.getActor());
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 500})
        void shouldReturnBadRequestAndInvokeRemainingInstances_WhenLoggerResetPartiallyFails(int failureStatusCode) {
            // given.
            failingLoggerUpdateStatusCode.set(failureStatusCode);
            LogLevelLoggerBulkResetRequest requestBody = new LogLevelLoggerBulkResetRequest(
                    List.of(failingInstanceId, siblingInstanceId), "reset.logger.name");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asViewer()
                    .postForEntity("/api/external/loggers/logger/reset", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + failingInstanceId + "/axelix-loggers/logger/reset.logger.name/reset",
                            "/" + siblingInstanceId + "/axelix-loggers/logger/reset.logger.name/reset");
        }

        @Test
        void shouldReturnBadRequestAndInvokeRemainingInstances_WhenResettingOnUnknownInstance() {
            // given.
            LogLevelLoggerBulkResetRequest requestBody = new LogLevelLoggerBulkResetRequest(
                    List.of("unknown-logger-instance", siblingInstanceId), "reset.logger.name");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asViewer()
                    .postForEntity("/api/external/loggers/logger/reset", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactly("/" + siblingInstanceId + "/axelix-loggers/logger/reset.logger.name/reset");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRejectMissingInstanceIds_WhenResettingLoggingLevel(List<String> instanceIds) {
            // given.
            // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", instanceIds);
            requestBody.put("loggerName", "reset.logger.name");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asViewer()
                    .postForEntity("/api/external/loggers/logger/reset", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @Test
        void shouldRejectBlankInstanceIds_WhenResettingLoggingLevel() {
            // given.
            // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
            Map<String, Object> requestBody =
                    Map.of("instanceIds", List.of("", " "), "loggerName", "reset.logger.name");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asViewer()
                    .postForEntity("/api/external/loggers/logger/reset", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = " ")
        void shouldRejectMissingLoggerName_WhenResettingLoggingLevel(String loggerName) {
            // given.
            // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", List.of(activeInstanceId));
            requestBody.put("loggerName", loggerName);

            // when.
            ResponseEntity<String> response = restTemplate
                    .asViewer()
                    .postForEntity("/api/external/loggers/logger/reset", requestBody, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.LOGGER_GROUP_CHANGE,
                        "/api/external/loggers/00000000-0000-0000-0000-000000000001/group/groupName"),
                new TestableMasterWebEndpoint(MasterWebEndpoints.LOGGERS_BULK_CHANGE, "/api/external/loggers/logger"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.LOGGERS_BULK_RESET, "/api/external/loggers/logger/reset"));
    }
}
