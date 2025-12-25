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
package com.nucleonforge.axile.master.api.loggers;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.LoggersApi;
import com.nucleonforge.axile.master.api.UserApi;
import com.nucleonforge.axile.master.api.request.LogLevelChangeRequest;
import com.nucleonforge.axile.master.service.auth.CookieService;
import com.nucleonforge.axile.master.service.auth.UserLoginService;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.utils.TestObjectFactory;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LoggersApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "axile.master.auth.type=none")
public class LoggersApiSetLoggingLevelByGroupNameTest {

    @MockBean
    private UserApi userApi;

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private CookieService cookieService;

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplate restTemplate;

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

                if (path.equals("/" + activeInstanceId + "/loggers/test")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldSetLoggingLevelByGroupName() {
        String groupName = "test";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO");

        registry.register(TestObjectFactory.createInstance(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // when.
        ResponseEntity<String> body = restTemplate.postForEntity(
                "/api/axile/loggers/{instanceId}/group/{groupName}",
                requestBody,
                String.class,
                activeInstanceId,
                groupName);

        // then.
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();
        String groupName = "test";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO");

        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<?> response = restTemplate.postForEntity(
                "/api/axile/loggers/{instanceId}/group/{groupName}", requestBody, Void.class, instanceId, groupName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = "unregistered-loggers-group-instance";
        String groupName = "test";
        LogLevelChangeRequest requestBody = new LogLevelChangeRequest("INFO");

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/loggers/{instanceId}/group/{groupName}",
                requestBody,
                EndpointInvocationException.class,
                instanceId,
                groupName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
