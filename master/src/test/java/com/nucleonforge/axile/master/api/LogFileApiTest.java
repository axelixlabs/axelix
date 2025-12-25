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
package com.nucleonforge.axile.master.api;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.auth.CookieService;
import com.nucleonforge.axile.master.service.auth.UserLoginService;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.utils.TestObjectFactory;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LogFileApi}.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "axile.master.auth.type=none")
class LogFileApiTest {

    @MockBean
    private UserApi userApi;

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private CookieService cookieService;

    private static final String logContent =
            """
            2025-11-12T14:05:13.795+05:00  INFO 1868 --- [main] o.s.s.petclinic.PetClinicApplication: Starting PetClinicApplication using Java 17.0.16 with PID 1868 (C:\\Project\\spring-petclinic\\target\\classes started in C:\\Project\\spring-petclinic)
            2025-11-12T14:05:13.795+05:00  INFO 1868 --- [main] o.s.s.petclinic.PetClinicApplication     : No active profile set, falling back to 1 default profile: "default"
            2025-11-12T14:05:14.382+05:00  INFO 1868 --- [main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
            2025-11-12T14:05:14.429+05:00  INFO 1868 --- [main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 30 ms. Found 3 JPA repository interfaces.
            2025-11-12T14:05:14.531+05:00  INFO 1868 --- [main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=4c03ca02-57eb-3d79-a155-785dae504167
            """;

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
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/logfile")) {
                    String rangeHeader = request.getHeader("Range");

                    if (rangeHeader != null && rangeHeader.startsWith("bytes=0-151")) {
                        String partialContent = logContent.substring(0, 152);
                        return new MockResponse()
                                .setBody(partialContent)
                                .addHeader("Content-Type", "text/plain;charset=UTF-8")
                                .setResponseCode(206)
                                .addHeader("Content-Range", "bytes 0-151/" + logContent.length());
                    } else {
                        return new MockResponse()
                                .setBody(logContent)
                                .addHeader("Content-Type", "text/plain;charset=UTF-8");
                    }
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnLogFileAsPlainText() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/logfile/{instanceId}", String.class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getBody()).contains(logContent);
    }

    @Test
    void shouldReturnLogFileSupportRangeHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Range", "bytes=0-151");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/axile/logfile/{instanceId}", HttpMethod.GET, entity, String.class, activeInstanceId);

        String expectedPartialContent = logContent.substring(0, 152);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getBody()).contains(expectedPartialContent);
        assertThat(response.getBody()).doesNotContain(logContent);
    }

    @Test
    void shouldReturnInternalServerErrorForInvalidInstance() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/logfile/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<InstanceNotFoundException> response = restTemplate.getForEntity(
                "/api/axile/env/feed/{instanceId}", InstanceNotFoundException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
